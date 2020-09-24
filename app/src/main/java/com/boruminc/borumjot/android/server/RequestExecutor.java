package com.boruminc.borumjot.android.server;

class RequestExecutor {
    private String[] params;
    private String charset;
    private String query;

    RequestExecutor(String... p) {
        params = p;
    }

    /**
     * Gets a parameter from the params array
     * @param index The index in the params array
     * @return The value of the parameter at the index
     */
    String getParam(int index) {
        return params[index];
    }

    String getQuery() {
        return query;
    }

    void setQuery(String newQuery) {
        query = newQuery;
    }

    void initialize() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            charset = java.nio.charset.StandardCharsets.UTF_8.name();
        } else {
            charset = "UTF-8";
        }
    }

    String getCharset() {
        return charset;
    }

    void setCharset(String newCharset) {
        charset = newCharset;
    }
}
