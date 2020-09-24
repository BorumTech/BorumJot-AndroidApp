package com.boruminc.borumjot.android.server;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class LoginUser extends RequestExecutor implements Callable<JSONObject> {
    private String query;

    public LoginUser(String...p) {
        super(p);
    }

    void initialize() {
        super.initialize();
        try {
            query = String.format("email=%s&password=%s",
                    URLEncoder.encode(getParam(0), getCharset()),
                    URLEncoder.encode(getParam(1), getCharset()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public JSONObject call() {
        initialize(); // Initialize

        // Setup
        String url = "https://api.jot.bforborum.com/api/v1/login?app_api_key=";
        url = url.concat(Uri.encode("9ds89d8as9das9"));

        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setDoOutput(true); // Triggers POST.
            connection.setRequestProperty("Accept-Charset", getCharset());
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=".concat(getCharset()));
            connection.connect();

            try (OutputStream output = connection.getOutputStream()) {
                output.write(query.getBytes(getCharset()));
            }

            int status = ((HttpURLConnection) connection).getResponseCode();
            InputStream response;
            if (status == 200) {
                response = connection.getInputStream();
            } else {
                response = ((HttpURLConnection) connection).getErrorStream();
            }

            String contentType = connection.getHeaderField("Content-Type");
            String responseCharset = null;

            for (String param : contentType.replace(" ", "").split(";")) {
                if (param.startsWith("charset=")) {
                    responseCharset = param.split("=", 2)[1];
                    break;
                }
            }

            if (responseCharset != null) {
                StringBuilder jsonResponse = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response, responseCharset))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonResponse.append(line);
                    }
                }

                Log.d("JSON RESPONSE", jsonResponse.toString());

                return new JSONObject(jsonResponse.toString());
            }

            return new JSONObject("{\"error\": {\"message\": \"Charset was null\"}");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
