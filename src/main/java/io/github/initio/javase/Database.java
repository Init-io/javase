package io.github.initio.javase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A simple Database library to interact with Firebase Realtime Database.
 * Provides methods to perform CRUD operations: `get`, `put`, `update`, and `remove`.
 */
public class Database {
    private Server server;
    private String databaseUrl;
    private ExecutorService executorService;
    private String lastError;

    /**
     * Constructor to initialize the Database instance.
     *
     * @param server The Server object containing the Firebase Database URL.
     */
    public Database(Server server) {
        this.server = server;
        this.databaseUrl = server.getDatabaseUrl();
        this.executorService = Executors.newCachedThreadPool();
        this.lastError = null;
    }

    /**
     * Fetches data from the Firebase Realtime Database.
     *
     * @param path    The path to the data in the database.
     * @param idToken The authentication token (optional, can be empty).
     * @return The JSON string response from the database or null if an error occurs.
     */
    public String get(String path, String idToken) {
        try {
            //System.out.println("DEBUG: get() called with path = " + path);
            validatePath(path);
            String result = executeTask(() -> getData(path, idToken));
            if (result == null) throw new IOException("Server returned null for get request");
            clearError();
            return result;
        } catch (Exception e) {
            captureError(e);
            return null;
        }
    }

    /**
     * Adds or replaces data in the Firebase Realtime Database.
     *
     * @param path    The path where the data will be stored.
     * @param key     The key for the data to be stored.
     * @param value   The value to be stored (can be String or Integer).
     * @param idToken The authentication token (optional, can be empty).
     * @return The JSON string response from the database or null if an error occurs.
     */
    public String put(String path, String key, Object value, String idToken) {
        try {
            validatePath(path);

            String fullPath = path + "/" + key;

            // Fetch existing data
            String existingDataJson = get(fullPath, idToken);
            Object existingValue = null;

            if (existingDataJson != null && !existingDataJson.equals("null")) {
                existingValue = new JSONTokener(existingDataJson).nextValue();
            }

            // Case 1: No existing data, store as a single string/int
            if (existingValue == null) {
                if (value instanceof String || value instanceof Integer) {
                    return executeTask(() -> putData(fullPath, "\"" + value + "\"", idToken));
                }
                throw new IllegalArgumentException("First value must be String or Integer");
            }

            JSONArray jsonArray;

            // Case 2: If existing value is a single string/int, convert to an array
            if (existingValue instanceof String || existingValue instanceof Integer) {
                jsonArray = new JSONArray();
                jsonArray.put(existingValue); // Convert old value into an array
            } else if (existingValue instanceof JSONArray) {
                jsonArray = (JSONArray) existingValue; // Load existing array
            } else {
                jsonArray = new JSONArray();
                throw new IllegalStateException("Unexpected existing data format");
            }

            // Append new value to array
            if (value instanceof String || value instanceof Integer) {
                jsonArray.put(value);
                return executeTask(() -> putData(fullPath, jsonArray.toString(), idToken));
            }
            else if (value instanceof List<?>) {
                // Convert list to indexed object format
                JSONObject jsonObject = new JSONObject();
                int index = 0;
                for (Object obj : (List<?>) value) {
                    jsonObject.put(String.valueOf(index++), obj);
                }
                return executeTask(() -> putData(fullPath, jsonObject.toString(), idToken));
            }
            else {
                throw new IllegalArgumentException("Value must be String, Integer, or List");
            }
        } catch (Exception e) {
            captureError(e);
            return null;
        }
    }


    private String patchData(String path, String data, String token) throws IOException {
        URL url = new URL(databaseUrl + "/" + path + ".json?auth=" + token);

        // Fetch existing data
        String existingDataJson = get(path, token);
        JSONArray updatedJsonArray = new JSONArray();

        if (existingDataJson != null && !existingDataJson.equals("null")) {
            Object existingValue = new JSONTokener(existingDataJson).nextValue();
            if (existingValue instanceof JSONArray) {
                updatedJsonArray = (JSONArray) existingValue;
            } else {
                updatedJsonArray.put(existingValue); // Wrap existing value in array if not already an array
            }
        }

        // Append new data (either as an array or single value)
        Object newData = new JSONTokener(data).nextValue();
        if (newData instanceof JSONArray) {
            for (int i = 0; i < ((JSONArray) newData).length(); i++) {
                Object item = ((JSONArray) newData).get(i);
                if (!updatedJsonArray.toList().contains(item)) {
                    updatedJsonArray.put(item);
                }
            }
        } else {
            if (!updatedJsonArray.toList().contains(newData)) {
                updatedJsonArray.put(newData);
            }
        }

        // Send PATCH request with the updated array
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PATCH");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(updatedJsonArray.toString().getBytes());
            os.flush();
        }

        return handleResponse(connection);
    }



    /**
     * Appends a JSONObject as the next child (e.g., 0,1,2...) under the given path.
     *
     * @param path     The path to the parent node (e.g., "notifications").
     * @param json     The JSONObject to append.
     * @param idToken  The authentication token (optional, can be empty).
     * @return The JSON string response from the database or null if an error occurs.
     */
    /**
     * Pushes a JSONObject under the given path with a unique Firebase key (e.g., -NxABC...).
     *
     * @param path     The parent node path (e.g., "notifications").
     * @param json     The JSONObject to push.
     * @param idToken  The authentication token (optional, can be empty).
     * @return The Firebase-generated key response, or null if an error occurs.
     */
    public String pushJson(String path, JSONObject json, String idToken) {
        try {
            validatePath(path);
            return executeTask(() -> postData(path, json.toString(), idToken));
        } catch (Exception e) {
            captureError(e);
            return null;
        }
    }

    /**
     * Updates data at a specified path in the Firebase Realtime Database.
     *
     * @param path    The path where the data will be updated.
     * @param key     The key for the data to be updated.
     * @param value   The new value to be stored (can be String or Integer).
     * @param idToken The authentication token (optional, can be empty).
     * @return The JSON string response from the database or null if an error occurs.
     */
    public String update(String path, String key, Object value, String idToken) {
        try {
            validatePath(path);
            String data = createJsonPayload(key, value);
            String result = executeTask(() -> updateData(path, data, idToken));
            clearError();
            return result;
        } catch (Exception e) {
            captureError(e);
            return null;
        }
    }

    /**
     * Removes data from the Firebase Realtime Database.
     *
     * @param path    The path to the data to be removed.
     * @param idToken The authentication token (optional, can be empty).
     * @return The JSON string response from the database or null if an error occurs.
     */
    public String remove(String path, String idToken) {
        try {
            //System.out.println("DEBUG: remove() called with path = " + path);
            validatePath(path);
            String result = executeTask(() -> removeData(path, idToken));
            if (result == null) throw new IOException("Server returned null for remove request");
            clearError();
            return result;
        } catch (Exception e) {
            captureError(e);
            return null;
        }
    }

    /**
     * Returns the last error message, if any.
     *
     * @return The last error message or "No error" if there is no error.
     */
    public String error() {
        return lastError != null ? lastError : "No error";
    }

    /**
     * Executes a task in a separate thread and returns the result.
     *
     * @param task The task to be executed.
     * @return The result of the task execution or null if an error occurs.
     */
    private String executeTask(Callable<String> task) {
        try {
            //System.out.println("DEBUG: Task started");
            Future<String> future = executorService.submit(task);
            String result = future.get();
            //System.out.println("DEBUG: Task result = " + result);
            return result;
        } catch (Exception e) {
            lastError = "Task execution error: " + e.getMessage();
            //System.out.println("DEBUG: Task execution failed: " + lastError);
            return null;
        }
    }

    // --- Private methods for HTTP operations ---
    private String getData(String path, String token) throws IOException {
        URL url = new URL(databaseUrl + "/" + path + ".json?auth=" + token);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        return handleResponse(connection);
    }

    private String postData(String path, String data, String token) throws IOException {
        URL url = new URL(databaseUrl + "/" + path + ".json?auth=" + token);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(data.getBytes());
            os.flush();
        }

        return handleResponse(connection); // returns: {"name":"-NxABC..."}
    }

    public List<JSONObject> getJsonList(String path, String idToken) {
        List<JSONObject> list = new ArrayList<>();
        try {
            String rawJson = get(path, idToken);
            if (rawJson == null || rawJson.equals("null")) return list;

            JSONObject jsonObject = new JSONObject(rawJson);
            for (String key : jsonObject.keySet()) {
                Object obj = jsonObject.get(key);
                if (obj instanceof JSONObject) {
                    list.add((JSONObject) obj);
                }
            }
        } catch (Exception e) {
            captureError(e);
        }
        return list;
    }


    private String putData(String path, String data, String token) throws IOException {
        URL url = new URL(databaseUrl + "/" + path + ".json?auth=" + token);

        // Log URL and data for debugging
        System.out.println("Sending PUT request to: " + url);
        System.out.println("Payload being sent: " + data);

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

        connection.setRequestMethod("POST");
        connection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
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

    // --- Helper Methods ---
    private String handleResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        InputStream inputStream = (responseCode == 200) ? connection.getInputStream() : connection.getErrorStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            if (responseCode != 200) {
                lastError = "HTTP Error " + responseCode + ": " + response.toString();
                //System.out.println("DEBUG: Error Response Code = " + responseCode);
                throw new IOException(lastError);
            }

            if (response.toString().equals("null")) {
                lastError = "HTTP 200: Server returned null (No data at the specified path)";
                throw new IOException(lastError);
            }

            //System.out.println("DEBUG: Successful Response: " + response.toString());
            lastError = null;
            return response.toString();
        }
    }

    private void validatePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty.");
        }
        if (!path.matches("^[a-zA-Z0-9_/.-]+$")) {
            throw new IllegalArgumentException("Path contains invalid characters.");
        }
    }

    private String createJsonPayload(String key, Object value) {
        if (value instanceof String) {
            return "{\"" + key + "\":\"" + value + "\"}";
        } else if (value instanceof Integer) {
            return "{\"" + key + "\":" + value + "}";
        } else {
            throw new IllegalArgumentException("Value must be a String or Integer.");
        }
    }

    private void captureError(Exception e) {
        lastError = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
    }

    private void clearError() {
        lastError = null;
    }
}
