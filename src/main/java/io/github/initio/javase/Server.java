package io.github.initio.javase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

        try {
            // Test Firebase API with a dummy request to verify credentials
            URL testUrl = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + apiKey);
            HttpURLConnection connection = (HttpURLConnection) testUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Send dummy payload
            String payload = "{ \"email\": \"test@example.com\", \"password\": \"test1234\", \"returnSecureToken\": false }";
            connection.getOutputStream().write(payload.getBytes());
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            // Handle response
            handleResponse(connection);
            clearError(); // Clear error if initialization succeeds
            return true;
        } catch (IOException e) {
            captureError(e);
            return false;
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

    /**
     * Gets the API key for the server.
     *
     * @return The API key.
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Gets the authentication domain for the server.
     *
     * @return The authentication domain.
     */
    public String getAuthDomain() {
        return authDomain;
    }

    /**
     * Gets the database URL for the server.
     *
     * @return The database URL.
     */
    public String getDatabaseUrl() {
        return databaseUrl;
    }

    /**
     * Gets the storage bucket URL for the server.
     *
     * @return The storage bucket URL.
     */
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
