package com.github.initio.javase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Handles user authentication and management tasks with Firebase Authentication services.
 * Includes functionalities like signing up, signing in, resetting password, verifying user email, etc.
 */
public class Authenticate {

    private Server server;
    private String apiKey;
    private String authToken; // Stores the idToken
    private ExecutorService executorService;
    private String lastError;

    /**
     * Constructs an instance of Authenticate.
     *
     * @param server The server configuration containing the API key.
     */
    public Authenticate(Server server) {
        this.server = server;
        this.apiKey = server.getApiKey();
        this.authToken = null; // Starts as null
        this.executorService = Executors.newCachedThreadPool();
        this.lastError = null; // Tracks the last error
    }

    /**
     * Signs up a user with the provided email and password.
     *
     * @param email The email of the user.
     * @param password The password for the user.
     * @return The response from the sign-up operation.
     */
    public String signUp(String email, String password) {
        try {
            String response = executeTask(() -> signUpWithEmailAndPassword(email, password));
            authToken = extractIdToken(response); // Extract and store idToken if successful
            clearError();
            return response;
        } catch (Exception e) {
            captureError(e);
            return null;
        }
    }

    /**
     * Signs in a user with the provided email and password.
     *
     * @param email The email of the user.
     * @param password The password for the user.
     * @return The response from the sign-in operation.
     */
    public String signIn(String email, String password) {
        try {
            String response = executeTask(() -> signInWithEmailPassword(email, password));
            authToken = extractIdToken(response); // Extract and store idToken if successful
            clearError();
            return response;
        } catch (Exception e) {
            captureError(e);
            return null;
        }
    }

    /**
     * Returns the current authentication token (idToken).
     *
     * @return The current idToken.
     */
    public String getIdToken() {
        return authToken;
    }

    /**
     * Resets the password for the user with the provided email.
     *
     * @param email The email of the user whose password needs to be reset.
     * @return The response from the reset password operation.
     */
    public String resetUser(String email) {
        try {
            String response = executeTask(() -> resetPassword(email));
            clearError();
            return response;
        } catch (Exception e) {
            captureError(e);
            return null;
        }
    }

    /**
     * Sends an email verification request to the user with the provided idToken.
     *
     * @param idToken The idToken of the user.
     * @return The response from the email verification operation.
     */
    public String verifyUser(String idToken) {
        try {
            String response = executeTask(() -> sendEmailVerification(idToken));
            clearError();
            return response;
        } catch (Exception e) {
            captureError(e);
            return null;
        }
    }

    /**
     * Checks if the user with the provided idToken has verified their email.
     *
     * @param idToken The idToken of the user.
     * @return "Verified" if the email is verified, otherwise "Not Verified".
     */
    public String isVerified(String idToken) {
        try {
            String response = executeTask(() -> checkEmailVerified(idToken));
            clearError();
            return response;
        } catch (Exception e) {
            captureError(e);
            return null;
        }
    }

    /**
     * Removes a user with the provided idToken.
     *
     * @param idToken The idToken of the user to remove.
     * @return The response from the user removal operation.
     */
    public String removeUser(String idToken) {
        try {
            String response = executeTask(() -> deleteUser(idToken));
            clearError();
            return response;
        } catch (Exception e) {
            captureError(e);
            return null;
        }
    }

    /**
     * Gets the unique user ID for the user with the provided idToken.
     *
     * @param idToken The idToken of the user.
     * @return The unique user ID (localId).
     */
    public String getUserId(String idToken) {
        try {
            String response = executeTask(() -> getUserUniqueId(idToken));
            clearError();
            return response;
        } catch (Exception e) {
            captureError(e);
            return null;
        }
    }

    /**
     * Returns the last error message, if any.
     *
     * @return The last error message.
     */
    public String error() {
        return lastError != null ? lastError : "No error";
    }

    /**
     * Executes a task asynchronously and returns the result.
     *
     * @param task The task to execute.
     * @return The result of the task execution.
     * @throws Exception If the task throws an exception.
     */
    private String executeTask(Callable<String> task) throws Exception {
        Future<String> future = executorService.submit(task);
        return future.get();
    }

    private String signUpWithEmailAndPassword(String email, String password) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + apiKey);
        return sendRequest(url, email, password);
    }

    private String signInWithEmailPassword(String email, String password) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey);
        return sendRequest(url, email, password);
    }

    private String resetPassword(String email) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=" + apiKey);
        return sendRequestWithEmailAction(url, email, "PASSWORD_RESET");
    }

    private String sendEmailVerification(String idToken) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=" + apiKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String payload = "{ \"requestType\": \"VERIFY_EMAIL\", \"idToken\": \"" + idToken + "\" }";
        OutputStream os = connection.getOutputStream();
        os.write(payload.getBytes());
        os.flush();
        os.close();

        return handleResponse(connection);
    }

    private String checkEmailVerified(String idToken) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=" + apiKey);
        String response = sendRequestWithToken(url, idToken);

        JSONObject jsonResponse = new JSONObject(response);
        JSONArray users = jsonResponse.getJSONArray("users");
        JSONObject user = users.getJSONObject(0);
        return user.getBoolean("emailVerified") ? "Verified" : "Not Verified";
    }

    private String deleteUser(String idToken) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:delete?key=" + apiKey);
        return sendRequestWithToken(url, idToken);
    }

    private String getUserUniqueId(String idToken) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=" + apiKey);
        String response = sendRequestWithToken(url, idToken);

        JSONObject jsonResponse = new JSONObject(response);
        JSONArray users = jsonResponse.getJSONArray("users");
        return users.getJSONObject(0).getString("localId");
    }

    private String sendRequest(URL url, String email, String password) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String payload = "{ \"email\": \"" + email + "\", \"password\": \"" + password + "\", \"returnSecureToken\": true }";
        OutputStream os = connection.getOutputStream();
        os.write(payload.getBytes());
        os.flush();
        os.close();

        return handleResponse(connection);
    }

    private String sendRequestWithEmailAction(URL url, String email, String requestType) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String payload = "{ \"requestType\": \"" + requestType + "\", \"email\": \"" + email + "\" }";
        OutputStream os = connection.getOutputStream();
        os.write(payload.getBytes());
        os.flush();
        os.close();

        return handleResponse(connection);
    }

    private String sendRequestWithToken(URL url, String idToken) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String payload = "{ \"idToken\": \"" + idToken + "\" }";
        OutputStream os = connection.getOutputStream();
        os.write(payload.getBytes());
        os.flush();
        os.close();

        return handleResponse(connection);
    }

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

        if (responseCode != 200) {
            lastError = "HTTP Error " + responseCode + ": " + response.toString();
            throw new IOException(lastError);
        }

        lastError = null;
        return response.toString();
    }

    private String extractIdToken(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getString("idToken");
        } catch (Exception e) {
            return null;
        }
    }

    private void captureError(Exception e) {
        lastError = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
    }

    private void clearError() {
        lastError = null;
    }
}
