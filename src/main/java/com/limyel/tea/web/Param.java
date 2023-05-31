package com.limyel.tea.web;

import com.limyel.tea.core.util.AnnotationUtil;
import com.limyel.tea.web.annotation.PathVariable;
import com.limyel.tea.web.annotation.RequestBody;
import com.limyel.tea.web.annotation.RequestParam;
import com.limyel.tea.web.exception.WebException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class Param {

    public enum ParamType {
        PATH_VARIABLE,
        REQUEST_PARAM,
        REQUEST_BODY,
        SERVLET_VARIABLE;
    }

    private String name;
    private ParamType paramType;
    private Class<?> classType;
    private String defaultValue;

    public Param(String httpMethod, Method method, Parameter parameter, Annotation[] annotations) {
        PathVariable pathVariable = AnnotationUtil.getAnnotation(annotations, PathVariable.class);
        RequestParam requestParam = AnnotationUtil.getAnnotation(annotations, RequestParam.class);
        RequestBody requestBody = AnnotationUtil.getAnnotation(annotations, RequestBody.class);

        // 三个注解只能同时存在一个
        int total = (pathVariable == null ? 0 : 1) + (requestParam == null ? 0 : 1) + (requestBody == null ? 0 : 1);
        if (total > 1) {
            throw new WebException("@PathVariable, @RequestParam and @RequestBody can't be combined at method: " + method);
        }

        this.classType = parameter.getType();
        if (pathVariable != null) {
            this.name = pathVariable.value();
            this.paramType = ParamType.PATH_VARIABLE;
        }
        if (requestParam != null) {
            this.name = requestParam.value();
            this.defaultValue = requestParam.defaultValue();
            this.paramType = ParamType.REQUEST_PARAM;
        }
        if (requestBody != null) {
            this.paramType = ParamType.REQUEST_BODY;
        } else {
            this.paramType = ParamType.SERVLET_VARIABLE;
            if (this.classType != HttpServletRequest.class && this.classType != HttpServletRequest.class && this.classType != HttpSession.class
                    && this.classType != ServletContext.class) {
                throw new WebException("unsupport argument type: " + classType + " at method " + method);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParamType getParamType() {
        return paramType;
    }

    public void setParamType(ParamType paramType) {
        this.paramType = paramType;
    }

    public Class<?> getClassType() {
        return classType;
    }

    public void setClassType(Class<?> classType) {
        this.classType = classType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
