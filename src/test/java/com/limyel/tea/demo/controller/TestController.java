package com.limyel.tea.demo.controller;

import com.limyel.tea.web.annotation.GET;
import com.limyel.tea.web.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {

    @GET("/hello")
    public Map<String, String> hello() {
        return Map.of("name", "tea");
    }

}
