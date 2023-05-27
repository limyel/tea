package com.limyel.tea.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class FreeMarkerViewResolver implements ViewResolver {

    @Override
    public void init() {

    }

    @Override
    public void render(String viewName, Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
