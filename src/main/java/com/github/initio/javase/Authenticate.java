package com.github.initio.javase;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.github.initio.javase.Server;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class handles authentication operations for Firebase including sign-up, sign-in,
 * password reset, email verification, user removal, and user information retrieval.
 */
public class Authenticate {

    private Server server;
    private String apiKey;
    private String authToken; // To store the idToken
    private ExecutorService executorService;

    /**
     * Constructor for Authenticate class.
     *
     * @param server The Server instance to be used for authentication operations.
     */
    public Authenticate(Server server) {
        this.server = server;
        this.apiKey = server.getApiKey();
        this.authToken = server.getAuthDomain();
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * Signs up a user with email and password.
     *
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @return The response of the sign-up request.
     */
    public String signUp(String email, String password) {
        try {
            return executeTask(() -> signUpWithEmailAndPassword(email, password));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Signs in a user with email and password.
     *
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @return The response of the sign-in request, which includes the idToken.
     */
    public String signIn(String email, String password) {
        try {
            String response = executeTask(() -> signInWithEmailPassword(email, password));
            if (response.contains("idToken")) {
                authToken = extractIdToken(response); // Extract and save the idToken
            }
            return response;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Retrieves the idToken for the authenticated user.
     *
     * @return The idToken of the user.
     */
    public String getIdToken() {
        return authToken;
    }

    /**
     * Sends a password reset request to the provided email.
     *
     * @param email The email address of the user requesting a password reset.
     * @return The response of the password reset request.
     */
    public String resetUser(String email) {
        try {
            return executeTask(() -> resetPassword(email));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Sends an email verification request for the user with the provided idToken.
     *
     * @param idToken The idToken of the user to verify.
     * @return The response of the email verification request.
     */
    public String verifyUser(String idToken) {
        try {
            return executeTask(() -> sendEmailVerification(idToken));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Checks if the user's email is verified using the provided idToken.
     *
     * @param idToken The idToken of the user to check.
     * @return A message indicating whether the user's email is verified or not.
     */
    public String isVerified(String idToken) {
        try {
            return executeTask(() -> checkEmailVerified(idToken));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Removes the user associated with the provided idToken.
     *
     * @param idToken The idToken of the user to remove.
     * @return The response of the user removal request.
     */
    public String removeUser(String idToken) {
        try {
            return executeTask(() -> deleteUser(idToken));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Retrieves the unique user ID (localId) associated with the provided idToken.
     *
     * @param idToken The idToken of the user.
     * @return The unique user ID (localId) associated with the idToken.
     */
    public String getUserId(String idToken) {
        try {
            return executeTask(() -> getUserUniqueId(idToken));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Helper method to execute a task asynchronously using the ExecutorService.
     *
     * @param task The task to be executed asynchronously.
     * @return The result of the task execution.
     * @throws Exception If an exception occurs during task execution.
     */
    private String executeTask(Callable<String> task) throws Exception {
        Future<String> future = executorService.submit(task);
        return future.get();
    }

    /**
     * Helper method to handle HTTP requests for signing up a user.
     *
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @return The response of the sign-up request.
     * @throws IOException If an I/O error occurs during the HTTP request.
     */
    private String signUpWithEmailAndPassword(String email, String password) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + apiKey);
        return sendRequest(url, email, password);
    }

    /**
     * Helper method to handle HTTP requests for signing in a user.
     *
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @return The response of the sign-in request.
     * @throws IOException If an I/O error occurs during the HTTP request.
     */
    private String signInWithEmailPassword(String email, String password) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey);
        return sendRequest(url, email, password);
    }

    /**
     * Helper method to handle HTTP requests for sending a password reset email.
     *
     * @param email The email address of the user requesting a password reset.
     * @return The response of the password reset request.
     * @throws IOException If an I/O error occurs during the HTTP request.
     */
    private String resetPassword(String email) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=" + apiKey);
        return sendRequestWithEmailAction(url, email, "PASSWORD_RESET");
    }

    /**
     * Helper method to send a verification email to the user.
     *
     * @param idToken The idToken of the user to verify.
     * @return The response of the email verification request.
     * @throws IOException If an I/O error occurs during the HTTP request.
     */
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

    /**
     * Helper method to check if the user's email is verified using the provided idToken.
     *
     * @param idToken The idToken of the user to check.
     * @return A message indicating whether the user's email is verified or not.
     * @throws IOException If an I/O error occurs during the HTTP request.
     */
    private String checkEmailVerified(String idToken) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=" + apiKey);
        String response = sendRequestWithToken(url, idToken);

        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray users = jsonResponse.getJSONArray("users");
            JSONObject user = users.getJSONObject(0);
            boolean emailVerified = user.getBoolean("emailVerified");

            return emailVerified ? "Verified" : "Not Verified";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error checking email verification status";
        }
    }

    /**
     * Helper method to delete a user using the provided idToken.
     *
     * @param idToken The idToken of the user to delete.
     * @return The response of the user deletion request.
     * @throws IOException If an I/O error occurs during the HTTP request.
     */
    private String deleteUser(String idToken) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:delete?key=" + apiKey);
        return sendRequestWithToken(url, idToken);
    }

    /**
     * Helper method to retrieve the unique user ID (localId) using the provided idToken.
     *
     * @param idToken The idToken of the user.
     * @return The unique user ID (localId).
     * @throws IOException If an I/O error occurs during the HTTP request.
     */
    private String getUserUniqueId(String idToken) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=" + apiKey);
        String response = sendRequestWithToken(url, idToken);

        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray users = jsonResponse.getJSONArray("users");
            JSONObject user = users.getJSONObject(0);
            return user.getString("localId"); // Return the unique user ID (localId)
        } catch (Exception e) {
            e.printStackTrace();
            return "Error retrieving user ID";
        }
    }

    /**
     * Helper method to send an HTTP request for actions like password reset and email verification.
     *
     * @param url        The URL of the API endpoint.
     * @param email      The email of the user for which the action is being performed.
     * @param requestType The type of action (e.g., "PASSWORD_RESET").
     * @return The response of the request.
     * @throws IOException If an I/O error occurs during the HTTP request.
     */
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

    /**
     * Helper method to send an HTTP request with the idToken for operations like checking email verification.
     *
     * @param url     The URL of the API endpoint.
     * @param idToken The idToken of the user.
     * @return The response of the request.
     * @throws IOException If an I/O error occurs during the HTTP request.
     */
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

    /**
     * Helper method to send a generic HTTP request for user sign-up and sign-in actions.
     *
     * @param url     The URL of the API endpoint.
     * @param email   The email of the user.
     * @param password The password of the user.
     * @return The response of the request.
     * @throws IOException If an I/O error occurs during the HTTP request.
     */
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

    /**
     * Helper method to extract the idToken from a response.
     *
     * @param response The response JSON string.
     * @return The extracted idToken.
     */
    private String extractIdToken(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getString("idToken");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Handles the HTTP response by reading from the input or error stream based on the response code.
     *
     * @param connection The HttpURLConnection to read from.
     * @return The response body as a string.
     * @throws IOException If an I/O error occurs during the response reading process.
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
        return response.toString();
    }
}
