package com.boruminc.borumjot.android.server;

import android.net.Uri;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class ApiRequestExecutor implements Callable<JSONObject> {
    private String[] params;
    private String charset;
    private String query;
    private Map<String, String> requestHeaders;
    private String requestMethod;

    /**
     * Constructor for ApiRequestExecutor
     * @param p The POST parameters
     */
    protected ApiRequestExecutor(String... p) {
        params = p;
        requestHeaders = new HashMap<>();
        query = "";
    }

    protected void setQuery(String newQuery) {
        query = newQuery;
    }

    private String getCharset() {
        return charset;
    }

    protected void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * Default request headers and their values;
     * when overridden, the super method should NOT be called
     */
    private void loadRequestHeaders() {
        addRequestHeader("Accept-Charset", getCharset());
        addRequestHeader("Content-Type", "application/x-www-form-urlencoded;responseCharset=" + getCharset());
    }

    /**
     * Encodes the POST parameters of the request
     * @param unsafeQuery The unsanitized query with parameters provided directly by the user
     * @return An encoded, sanitized, safe query string
     */
    protected String encodePostQuery(String unsafeQuery) {
        try {
            Object[] encodedParams = new String[params.length];

            for (int i = 0; i < encodedParams.length; i++) {
                encodedParams[i] = URLEncoder.encode(params[i], getCharset());
            }

            return String.format(unsafeQuery, encodedParams); // Return the encoded query string
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Encodes a url
     * @param path The name of the file and any subsequent "/" parameters
     * @param urlParams The query string parameters (GET)
     * @return The encoded url
     */
    protected String encodeUrl(String path, String... urlParams) {
        String versionNumber = "v1";

        // Develop URL: https://borum-jot-api-git-develop.varun-singh.vercel.app/api/
        // Production URL: https://api.jot.bforborum.com/api/
        String safeUrl = "https://borum-jot-api-git-label-2.varun-singh.vercel.app/api/"
                .concat(versionNumber).concat("/")
                .concat(path).concat(urlParams.length > 0 ? "?" : "");

        // Loop through url query parameters, encode them, concat them to the url, and separate with "&"
        for (int i = 0; i < urlParams.length; i++) {
            String urlParam = urlParams[i];
            safeUrl = safeUrl
                    .concat(urlParam.substring(0, urlParam.indexOf("=") + 1))
                    .concat(Uri.encode(urlParam.substring(urlParam.indexOf("=") + 1))) // Exclude key, only get value
                    .concat(i < urlParams.length - 1 ? "&" : ""); // Add "&" if there are more parameters
        }
        Log.d("Safe url", safeUrl);

        return safeUrl;
    }

    protected void initialize() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            charset = java.nio.charset.StandardCharsets.UTF_8.name();
        } else {
            charset = "UTF-8";
        }
    }

    /**
     * Implements call method of Callable<JSONObject> interface
     * @return null
     */
    public JSONObject call() {
        initialize();
        loadRequestHeaders();
        return null;
    }

    /**
     * Connects to API using an InputStream
     * If a post request, uses OutputStream as well
     * @param url The url to which to open a connection
     * @return The response of the network request
     */
    protected JSONObject connectToApi(String url) {
        try {
            URLConnection connection = new URL(url).openConnection();

            for (Map.Entry<String, String> entry : getRequestHeaders().entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
                Log.d(entry.getKey(), entry.getValue());
            }

            ((HttpURLConnection) connection).setRequestMethod(requestMethod);
            if (query.contains("=")) {
                connection.setDoOutput(true);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(query.getBytes(getCharset()));
                }
            }

            int status = ((HttpURLConnection) connection).getResponseCode();
            InputStream response;
            if (status >= 200 && status < 300) response = connection.getInputStream();
            else response = ((HttpURLConnection) connection).getErrorStream();

            String contentType = connection.getHeaderField("Content-Type");
            String responseCharset = null;

            for (String param : contentType.replace(" ", "").split(";")) {
                if (param.startsWith("charset=")) {
                    responseCharset = param.split("=", 2)[1];
                    break;
                }
            }

            JSONObject toReturn = new JSONObject();
            JSONObject error = new JSONObject();
            error.put("message", "Charset was null");
            toReturn.put("ok", "false");
            toReturn.put("error", error);

            if (responseCharset != null) {
                StringBuilder jsonResponse = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response, responseCharset))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonResponse.append(line);
                    }
                }
                Log.d("JSON RESPONSE", String.valueOf(jsonResponse));
                toReturn = new JSONObject(jsonResponse.toString());
            }

            response.close();
            ((HttpURLConnection) connection).disconnect();

            return toReturn;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    protected void addRequestHeader(String name, String value) {
        if (!requestHeaders.containsKey(name))
            requestHeaders.put(name, value);
    }

    /**
     * Convenience method for adding the Authorization header for api key
     * @param userApiKey The value of the header, concatenated after "Basic "
     */
    protected void addAuthorizationHeader(String userApiKey) {
        this.addRequestHeader("Authorization", "Basic " + userApiKey);
    }
}
