package com.github.initio.javase;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 */
public class Database {
    private String databaseUrl;
    private ExecutorService executorService;

    /**
     *
     */
    public Database(Server server) {
        this.server = server;
        this.databaseUrl = server.getDatabaseUrl();
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     *
     */
    public String get(String path, String idToken) {
        try {
        } catch (Exception e) {
        }
    }

    /**
     *
     */
        try {
        } catch (Exception e) {
        }
    }

    /**
     *
     */
        try {
        } catch (Exception e) {
        }
    }

    /**
     * Removes data from the Firebase Realtime Database.
     *
     */
    public String remove(String path, String idToken) {
        try {
        } catch (Exception e) {
        }
    }

    /**
     *
     */
            Future<String> future = executorService.submit(task);
    }

    private String getData(String path, String token) throws IOException {
        URL url = new URL(databaseUrl + "/" + path + ".json?auth=" + token);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        return handleResponse(connection);
    }

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

    private String updateData(String path, String data, String token) throws IOException {
        URL url = new URL(databaseUrl + "/" + path + ".json?auth=" + token);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(data.getBytes());
            os.flush();
        }

        return handleResponse(connection);
    }

    private String removeData(String path, String token) throws IOException {
        URL url = new URL(databaseUrl + "/" + path + ".json?auth=" + token);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.connect();
        return handleResponse(connection);
    }

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
        }
    }
}
