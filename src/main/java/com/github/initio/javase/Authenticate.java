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
public class Authenticate {

    private Server server;
    private String apiKey;
    private ExecutorService executorService;

    /**
     *
     */
    public Authenticate(Server server) {
        this.server = server;
        this.apiKey = server.getApiKey();
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     *
     */
    public String signUp(String email, String password) {
        try {
        } catch (Exception e) {
        }
    }

    /**
     *
     */
    public String signIn(String email, String password) {
        try {
            String response = executeTask(() -> signInWithEmailPassword(email, password));
            return response;
        } catch (Exception e) {
        }
    }

    /**
     *
     */
    public String getIdToken() {
        return authToken;
    }

    /**
     *
     */
    public String resetUser(String email) {
        try {
        } catch (Exception e) {
        }
    }

    /**
     *
     */
    public String verifyUser(String idToken) {
        try {
        } catch (Exception e) {
        }
    }

    /**
     *
     */
    public String isVerified(String idToken) {
        try {
        } catch (Exception e) {
        }
    }

    /**
     *
     * @param idToken The idToken of the user to remove.
     */
    public String removeUser(String idToken) {
        try {
        } catch (Exception e) {
        }
    }

    /**
     *
     * @param idToken The idToken of the user.
     */
    public String getUserId(String idToken) {
        try {
        } catch (Exception e) {
    }
    }

    /**
     *
     * @return The result of the task execution.
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
    }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        OutputStream os = connection.getOutputStream();
        os.write(payload.getBytes());
        os.flush();
        os.close();

        return handleResponse(connection);
    }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        OutputStream os = connection.getOutputStream();
        os.write(payload.getBytes());
        os.flush();
        os.close();

        return handleResponse(connection);
    }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

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
        return response.toString();
    }
}
