package com.github.initio.javase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is responsible for handling server-side operations,
 * including initializing the server configuration and handling
 * HTTP responses from Firebase API requests.
 */
public class Server {

    private String apiKey;
    private String authDomain;
    private String databaseUrl;
    private String storageBucket;

    // ExecutorService for background tasks
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Variable to store the last error message
    private String lastError;

    /**
     * Initializes the server with configuration details.
     * Sends a test request to Firebase to verify the credentials.
     *
     * @param apiKey       The API key for the server.
     * @param authDomain   The authentication domain.
     * @param databaseUrl  The URL of the database.
     * @param storageBucket The storage bucket URL.
     * @return true if initialization is successful, false otherwise.
     */
    public boolean initialize(String apiKey, String authDomain, String databaseUrl, String storageBucket) {
        this.apiKey = apiKey;
        this.authDomain = authDomain;
        this.databaseUrl = databaseUrl;
        this.storageBucket = storageBucket;

        String testEmail = "testuser@example.com";
        String testPassword = "TestPassword123";

        try {
            // Create a test account to validate credentials
            String response = createTestAccount(testEmail, testPassword);

            // Extract the ID token of the test account
            String idToken = extractIdToken(response);

            // Immediately delete the test account
            deleteTestAccount(idToken);

            clearError(); // Clear error if initialization succeeds
            return true;
        } catch (IOException e) {
            captureError(e);
            return false;
        }
    }

    /**
     * Sends a request to create a test account.
     *
     * @param email    The email for the test account.
     * @param password The password for the test account.
     * @return The response from Firebase as a string.
     * @throws IOException If an I/O error occurs during the request.
     */
    private String createTestAccount(String email, String password) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + apiKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Send the request payload
        String payload = "{ \"email\": \"" + email + "\", \"password\": \"" + password + "\", \"returnSecureToken\": true }";
        OutputStream os = connection.getOutputStream();
        os.write(payload.getBytes());
        os.flush();
        os.close();

        return handleResponse(connection);
    }

    /**
     * Sends a request to delete the test account using its ID token.
     *
     * @param idToken The ID token of the test account to delete.
     * @throws IOException If an I/O error occurs during the request.
     */
    private void deleteTestAccount(String idToken) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:delete?key=" + apiKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Send the request payload
        String payload = "{ \"idToken\": \"" + idToken + "\" }";
        OutputStream os = connection.getOutputStream();
        os.write(payload.getBytes());
        os.flush();
        os.close();

        handleResponse(connection); // Handle any errors during account deletion
    }

    /**
     * Extracts the ID token from the Firebase API response.
     *
     * @param response The JSON response from Firebase.
     * @return The extracted ID token.
     */
    private String extractIdToken(String response) {
        try {
            org.json.JSONObject jsonResponse = new org.json.JSONObject(response);
            return jsonResponse.getString("idToken");
        } catch (Exception e) {
            return null;
        }
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

    /**
     * Captures an exception and stores its message as the last error.
     *
     * @param e The exception to capture.
     */
    private void captureError(Exception e) {
        lastError = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
    }

    /**
     * Clears the last error message.
     */
    private void clearError() {
        lastError = null;
    }

    /**
     * Returns the last error message if any operation failed.
     *
     * @return The last error message or "No error" if none exists.
     */
    public String error() {
        return lastError != null ? lastError : "No error";
    }

    // Getters for server properties

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

    /**
     * Shuts down the executor service to release resources.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
