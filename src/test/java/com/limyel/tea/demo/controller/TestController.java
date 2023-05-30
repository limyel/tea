package com.limyel.tea.demo.controller;

import com.limyel.tea.web.annotation.GET;

import java.util.Map;

public class TestController {

    @GET("/hello")
    public Map<String, String> hello() {
        return Map.of("name", "tea");
    }

}
