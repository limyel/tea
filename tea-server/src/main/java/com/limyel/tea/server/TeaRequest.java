package com.limyel.tea.server;

public interface TeaRequest {

    String getMethod();

    String getUrl();

    String getCookie(String key);

}
