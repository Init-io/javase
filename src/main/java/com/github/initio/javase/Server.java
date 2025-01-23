package com.github.initio.javase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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

    /**
     * Initializes the server with configuration details.
     *
     * @param apiKey       The API key for the server.
     * @param authDomain   The authentication domain.
     * @param databaseUrl  The URL of the database.
     * @param storageBucket The storage bucket URL.
     */
        this.apiKey = apiKey;
        this.authDomain = authDomain;
        this.databaseUrl = databaseUrl;
        this.storageBucket = storageBucket;
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
        return response.toString();
    }

    /**
     *
     */
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
