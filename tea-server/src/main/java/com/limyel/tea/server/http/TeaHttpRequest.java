package com.limyel.tea.server.http;

import com.limyel.tea.server.TeaRequest;

import java.util.HashMap;
import java.util.Map;

public class TeaHttpRequest implements TeaRequest {

    private String method;

    private String url;

    private Map<String, Cookie> cookieMap = new HashMap<>();
    private Map<String, String> headerMap = new HashMap<>();

    @Override
    public String getMethod() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public String getCookie(String key) {
        return null;
    }

}
