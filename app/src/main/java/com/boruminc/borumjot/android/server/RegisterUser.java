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

public class RegisterUser extends AsyncTask<String, Void, JSONObject> {
    private String charset;
    private String query;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            charset = java.nio.charset.StandardCharsets.UTF_8.name();
        } else {
            charset = "UTF-8";
        }
    }

    private void initialize(String...params) {
        try {
            query = String.format("first_name=%s&last_name=%s&email=%s&password=%s",
                    URLEncoder.encode(params[0], charset),
                    URLEncoder.encode(params[1], charset),
                    URLEncoder.encode(params[2], charset),
                    URLEncoder.encode(params[3], charset)
            );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        initialize(strings);

        // Setup
        String url = "https://api.jot.bforborum.com/api/v1/register?app_api_key=";
        url = url.concat(Uri.encode("9ds89d8as9das9"));

        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setDoOutput(true); // Triggers POST.
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
            connection.connect();

            try (OutputStream output = connection.getOutputStream()) {
                output.write(query.getBytes(charset));
            }

            int status = ((HttpURLConnection) connection).getResponseCode();
            InputStream response;
            if (status == 200) {
                response = connection.getInputStream();
            } else {
                response = ((HttpURLConnection) connection).getErrorStream();
            }

            String contentType = connection.getHeaderField("Content-Type");
            String charset = null;

            for (String param : contentType.replace(" ", "").split(";")) {
                if (param.startsWith("charset=")) {
                    charset = param.split("=", 2)[1];
                    break;
                }
            }

            if (charset != null) {
                StringBuilder jsonResponse = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response, charset))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                       jsonResponse.append(line);
                    }
                }

                Log.d("JSON", jsonResponse.toString());
                return new JSONObject(jsonResponse.toString());
            }

            return new JSONObject("{\"ok\": \"false\", \"error\": {\"message\": \"Charset was null\"}");
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
