package com.github.initio.javase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A class responsible for interacting with Firebase Storage for file upload, deletion, and retrieval operations.
 * This class allows uploading files to storage, retrieving download URLs, listing photos in storage, and deleting photos.
 * It provides methods to handle file storage tasks asynchronously to avoid blocking the main thread.
 */
public class Storage {

    private final Server server;
    private final Authenticate auth;
    private final String storageBucket;
    private final ExecutorService executorService;

    /**
     * Constructs a new Storage object with the given server and authentication details.
     *
     * @param server The server object containing server configuration and details.
     * @param auth   The Authenticate object for authentication purposes.
     */
    public Storage(Server server, com.github.initio.javase.Authenticate auth) {
        this.server = server;
        this.auth = auth;
        this.storageBucket = server.getStorageBucket();
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * Uploads a file to Firebase Storage.
     *
     * @param filePath     The local file path of the file to upload.
     * @param storagePath  The path in Firebase Storage where the file should be uploaded.
     * @return A response message indicating the result of the upload operation.
     */
    public String uploadFile(String filePath, String storagePath) {
        return executeTask(() -> uploadFileToStorage(filePath, storagePath));
    }

    /**
     * Retrieves the download URL for a file in Firebase Storage.
     *
     * @param storagePath The path of the file in Firebase Storage.
     * @return A response message containing the download URL of the file.
     */
    public String getFileUrl(String storagePath) {
        return executeTask(() -> getFileDownloadUrl(storagePath));
    }

    /**
     * Lists all photos in the given storage path.
     *
     * @param storagePath The path in Firebase Storage where photos are stored.
     * @return A response message containing a list of photo names in the storage path.
     */
    public String listPhotos(String storagePath) {
        return executeTask(() -> listAllPhotos(storagePath));
    }

    /**
     * Deletes a photo from Firebase Storage.
     *
     * @param storagePath The path in Firebase Storage where the photo is located.
     * @return A response message indicating whether the photo was deleted successfully or not.
     */
    public String deletePhoto(String storagePath) {
        return executeTask(() -> deletePhotoFromStorage(storagePath));
    }

    /**
     * Executes a task asynchronously and returns the result.
     *
     * @param task The task to execute.
     * @return The result of the task.
     */
    private String executeTask(Callable<String> task) {
        try {
            Future<String> future = executorService.submit(task);
            return future.get();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Uploads a file to Firebase Storage.
     *
     * @param filePath    The local file path of the file to upload.
     * @param storagePath The path in Firebase Storage where the file should be uploaded.
     * @return The response from Firebase after uploading the file.
     * @throws IOException If an error occurs during the file upload process.
     */
    private String uploadFileToStorage(String filePath, String storagePath) throws IOException {
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        @SuppressWarnings("NewApi") URL url = new URL("https://firebasestorage.googleapis.com/v0/b/" + storageBucket + "/o/" +
                URLEncoder.encode(storagePath, StandardCharsets.UTF_8).replace("+", "%20") +
                "?uploadType=media&name=" + file.getName());

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            fileInputStream.close();
        }

        return handleResponse(connection);
    }

    /**
     * Retrieves the download URL for a file in Firebase Storage.
     *
     * @param storagePath The path of the file in Firebase Storage.
     * @return The URL to download the file.
     * @throws IOException If an error occurs while fetching the download URL.
     */
    private String getFileDownloadUrl(String storagePath) throws IOException {
        @SuppressWarnings("NewApi") URL url = new URL("https://firebasestorage.googleapis.com/v0/b/" + storageBucket + "/o/" +
                URLEncoder.encode(storagePath, StandardCharsets.UTF_8).replace("+", "%20") + "?alt=media");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        return handleResponse(connection);
    }

    /**
     * Lists all photos in the specified Firebase Storage path.
     *
     * @param storagePath The path to list photos from in Firebase Storage.
     * @return A comma-separated string of photo names in the storage path.
     * @throws IOException If an error occurs while retrieving the list of photos.
     */
    private String listAllPhotos(String storagePath) throws IOException {
        if (!storagePath.endsWith("/")) {
            storagePath += "/";
        }

        @SuppressWarnings("NewApi") URL url = new URL("https://firebasestorage.googleapis.com/v0/b/" + storageBucket + "/o?prefix=" +
                URLEncoder.encode(storagePath, StandardCharsets.UTF_8).replace("+", "%20"));

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        String jsonResponse = handleResponse(connection);

        List<String> photoNames = new ArrayList<>();
        try {
            JSONObject responseObj = new JSONObject(jsonResponse);
            JSONArray itemsArray = responseObj.getJSONArray("items");

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                String fullName = item.getString("name");
                String imageName = fullName.substring(fullName.lastIndexOf('/') + 1);
                photoNames.add(imageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return String.join(", ", photoNames);
    }

    /**
     * Deletes a photo from Firebase Storage.
     *
     * @param storagePath The path of the photo to delete in Firebase Storage.
     * @return A response message indicating the success or failure of the deletion operation.
     * @throws IOException If an error occurs during the deletion process.
     */
    private String deletePhotoFromStorage(String storagePath) throws IOException {
        @SuppressWarnings("NewApi") String encodedPath = URLEncoder.encode(storagePath, StandardCharsets.UTF_8).replace("+", "%20");

        URL url = new URL("https://firebasestorage.googleapis.com/v0/b/" + storageBucket + "/o/" + encodedPath);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Authorization", "Bearer " + auth.getIdToken());
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            return "Photo deleted successfully";
        } else {
            String errorMessage = "Error deleting photo: " + responseCode;
            InputStream errorStream = connection.getErrorStream();

            if (errorStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorMessage += "\nError response: " + errorResponse.toString();
                }
            }

            return errorMessage;
        }
    }

    /**
     * Handles the HTTP response from Firebase Storage.
     *
     * @param connection The HttpURLConnection object used for the request.
     * @return The response message from Firebase Storage.
     * @throws IOException If an error occurs while reading the response.
     */
    private String handleResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        InputStream inputStream = (responseCode == HttpURLConnection.HTTP_OK) ? connection.getInputStream() : connection.getErrorStream();

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
