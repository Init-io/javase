package com.github.initio.javase;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class is responsible for interacting with the Firebase Realtime Database.
 * It provides methods for reading (GET), writing (PUT), updating (PATCH), and deleting (DELETE) data
 * from the database. It uses an ExecutorService to execute these operations asynchronously.
 */
public class Database {
    private com.github.initio.javase.Server server;
    private String databaseUrl;
    private ExecutorService executorService;

    /**
     * Constructor that initializes the Database with the given Server configuration.
     *
     * @param server The Server object containing configuration details (e.g., database URL).
     */
    public Database(Server server) {
        this.server = server;
        this.databaseUrl = server.getDatabaseUrl();
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * Retrieves data from the Firebase Realtime Database.
     *
     * @param path    The path in the database to retrieve data from.
     * @param idToken The Firebase ID token for authentication.
     * @return The data retrieved from the database as a String, or an error message.
     */
    public String get(String path, String idToken) {
        try {
            return executeTask(() -> getData(path, idToken));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Writes data to the Firebase Realtime Database.
     *
     * @param path    The path in the database to write data to.
     * @param key     The key of the data to store.
     * @param value   The value to store in the database.
     * @param idToken The Firebase ID token for authentication.
     * @return The response from the database or an error message.
     */
    public String put(String path, String key, String value, String idToken) {
        try {
            String data = "{\"" + key + "\":\"" + value + "\"}";
            return executeTask(() -> putData(path, data, idToken));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Updates data in the Firebase Realtime Database.
     *
     * @param path    The path in the database to update data in.
     * @param key     The key of the data to update.
     * @param value   The new value to store in the database.
     * @param idToken The Firebase ID token for authentication.
     * @return The response from the database or an error message.
     */
    public String update(String path, String key, String value, String idToken) {
        try {
            String data = "{\"" + key + "\":\"" + value + "\"}";
            return executeTask(() -> updateData(path, data, idToken));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Removes data from the Firebase Realtime Database.
     *
     * @param path    The path in the database to remove data from.
     * @param idToken The Firebase ID token for authentication.
     * @return The response from the database or an error message.
     */
    public String remove(String path, String idToken) {
        try {
            return executeTask(() -> removeData(path, idToken));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Helper method to execute tasks asynchronously.
     *
     * @param task The task to execute.
     * @return The result of the task execution.
     * @throws Exception If an error occurs while executing the task.
     */
    private String executeTask(Callable<String> task) throws Exception {
        Future<String> future = executorService.submit(task);
        return future.get();
    }

    // Database operations

    /**
     * Retrieves data from the Firebase Realtime Database.
     *
     * @param path    The path in the database to retrieve data from.
     * @param token   The Firebase ID token for authentication.
     * @return The data retrieved from the database.
     * @throws IOException If an I/O error occurs while making the request.
     */
    private String getData(String path, String token) throws IOException {
        URL url = new URL(databaseUrl + "/" + path + ".json?auth=" + token);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        return handleResponse(connection);
    }

    /**
     * Writes data to the Firebase Realtime Database.
     *
     * @param path    The path in the database to write data to.
     * @param data    The data to write to the database.
     * @param token   The Firebase ID token for authentication.
     * @return The response from the database.
     * @throws IOException If an I/O error occurs while making the request.
     */
    private String putData(String path, String data, String token) throws IOException {
        URL url = new URL(databaseUrl + "/" + path + ".json?auth=" + token);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(data.getBytes());
            os.flush();
        }

        return handleResponse(connection);
    }

    /**
     * Updates data in the Firebase Realtime Database.
     *
     * @param path    The path in the database to update data in.
     * @param data    The data to update in the database.
     * @param token   The Firebase ID token for authentication.
     * @return The response from the database.
     * @throws IOException If an I/O error occurs while making the request.
     */
    private String updateData(String path, String data, String token) throws IOException {
        URL url = new URL(databaseUrl + "/" + path + ".json?auth=" + token);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PATCH");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(data.getBytes());
            os.flush();
        }

        return handleResponse(connection);
    }

    /**
     * Removes data from the Firebase Realtime Database.
     *
     * @param path    The path in the database to remove data from.
     * @param token   The Firebase ID token for authentication.
     * @return The response from the database.
     * @throws IOException If an I/O error occurs while making the request.
     */
    private String removeData(String path, String token) throws IOException {
        URL url = new URL(databaseUrl + "/" + path + ".json?auth=" + token);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.connect();
        return handleResponse(connection);
    }

    /**
     * Handles the HTTP response from a Firebase Realtime Database API request.
     *
     * @param connection The HTTP connection to handle the response from.
     * @return The response body as a string.
     * @throws IOException If an I/O error occurs while reading the response.
     */
    private String handleResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        InputStream inputStream = (responseCode == 200) ? connection.getInputStream() : connection.getErrorStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }
}
