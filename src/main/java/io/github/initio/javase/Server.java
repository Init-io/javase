package io.github.initio.javase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

public class Server {

    private String apiKey;
    private String authDomain;
    private String databaseUrl;
    private String storageBucket;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String lastError;

    public boolean initialize(String apiKey, String authDomain, String databaseUrl, String storageBucket) {
        this.apiKey = apiKey;
        this.authDomain = authDomain;
        this.databaseUrl = databaseUrl;
        this.storageBucket = storageBucket;

        String idToken = null; // Stores the idToken for the dummy account

        try {

            // Step 1: Check for existing dummy account
            try {
                idToken = signInDummyAccount(apiKey); // Try signing in with the dummy account

            } catch (IOException e) {
                // Handle specific errors
                if (lastError != null && lastError.contains("INVALID_LOGIN_CREDENTIALS")) {
                    idToken = createDummyAccount(apiKey); // Create a new dummy account

                } else if (lastError != null && lastError.contains("EMAIL_EXISTS")) {
                    idToken = signInDummyAccount(apiKey); // Log in to get the idToken
                } else {
                    throw e; // Re-throw other unexpected errors
                }
            }

            // Step 2: Perform initialization
            clearError();
            return true;

        } catch (IOException e) {
            captureError(e);
            System.err.println("Error during initialization: " + e.getMessage());
            return false;

        } finally {
            // Step 3: Cleanup - delete the dummy account after initialization
            if (idToken != null) {
                try {
                    deleteDummyAccount(apiKey, idToken);
                } catch (IOException e) {
                    captureError(e);
                }
            }
        }
    }




    /**
     * Signs in to the dummy Firebase account if it exists.
     *
     * @param apiKey The Firebase API key.
     * @return The idToken if the account exists and login is successful, null otherwise.
     * @throws IOException If an I/O error occurs.
     */
    private String signInDummyAccount(String apiKey) throws IOException {

        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String payload = "{ \"email\": \"test@example.com\", \"password\": \"test1234\", \"returnSecureToken\": true }";
        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.getBytes());
            os.flush();
        }

        try {
            String response = handleResponse(connection);
            System.out.println("Sign-in response: " + response); // Debugging log
            String idToken = extractIdToken(response);
            if (idToken != null) {
                System.out.println("Sign-in successful. idToken: " + idToken);
            } else {
                System.out.println("Failed to extract idToken from sign-in response.");
            }
            return idToken;
        } catch (IOException e) {
            if (lastError != null && lastError.contains("EMAIL_NOT_FOUND")) {
                System.out.println("Dummy account does not exist."); // Debugging log
                return null;
            }
            throw e;
        }
    }


    /**
     * Creates a dummy Firebase account and returns the idToken.
     *
     * @param apiKey The Firebase API key.
     * @return The idToken of the created account.
     * @throws IOException If an I/O error occurs.
     */
    private String createDummyAccount(String apiKey) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + apiKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Send dummy payload
        String payload = "{ \"email\": \"test@example.com\", \"password\": \"test1234\", \"returnSecureToken\": true }";
        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.getBytes());
            os.flush();
        }

        String response = handleResponse(connection);
        return extractIdToken(response);
    }

    /**
     * Deletes a dummy Firebase account using the given idToken.
     *
     * @param apiKey  The Firebase API key.
     * @param idToken The idToken of the account to be deleted.
     * @throws IOException If an I/O error occurs.
     */
    private void deleteDummyAccount(String apiKey, String idToken) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:delete?key=" + apiKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Send payload with idToken
        String payload = "{ \"idToken\": \"" + idToken + "\" }";
        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.getBytes());
            os.flush();
        }

        handleResponse(connection);
    }

    /**
     * Handles the HTTP response from Firebase API requests.
     *
     * @param connection The HTTP connection to the Firebase API.
     * @return The response body as a string.
     * @throws IOException If an I/O error occurs while reading the response.
     */
    private String handleResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        InputStream inputStream = (responseCode == 200) ? connection.getInputStream() : connection.getErrorStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();

        if (responseCode != 200) {
            lastError = "HTTP Error " + responseCode + ": " + response.toString();
            throw new IOException(lastError);
        }

        return response.toString();
    }

    private String extractIdToken(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            if (json.has("idToken")) {
                return json.getString("idToken");
            } else {
                System.err.println("idToken not found in response.");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON response: " + e.getMessage());
            return null;
        }
    }

    private void captureError(Exception e) {
        lastError = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
    }

    private void clearError() {
        lastError = null;
    }

    public String error() {
        return lastError != null ? lastError : "No error";
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getAuthDomain() {
        return authDomain;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getStorageBucket() {
        return storageBucket;
    }
}
