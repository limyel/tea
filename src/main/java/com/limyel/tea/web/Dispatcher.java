package com.limyel.tea.web;

import com.limyel.tea.core.util.ObjectUtil;
import com.limyel.tea.web.annotation.Controller;
import com.limyel.tea.web.exception.WebException;
import com.limyel.tea.web.util.JsonUtil;
import com.limyel.tea.web.util.PathUtil;
import com.limyel.tea.web.util.WebUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dispatcher {

    public static final Result NOT_PROCESSED = new Result(false, null);
    private boolean returnVoid;
    private Pattern[] urlPatterns;
    private Object controller;
    private Method handlerMethod;
    private Param[] methodParameters;

    public Dispatcher(String httpMethod, Object controller, Method method, String urlPattern) {
        this.returnVoid = method.getReturnType() == void.class;
        Controller controllerAnno = controller.getClass().getAnnotation(Controller.class);
        for (int i = 0; i < controllerAnno.path().length; i++) {
            String path = controllerAnno.path()[i];
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            if (!urlPattern.startsWith("/")) {
                urlPattern = "/" + urlPattern;
            }
            this.urlPatterns[i] = PathUtil.compile(path + urlPattern);
        }
        this.controller = controller;
        this.handlerMethod = method;
        Parameter[] params = method.getParameters();
        Annotation[][] paramsAnnos = method.getParameterAnnotations();
        this.methodParameters = new Param[params.length];
        for (int i = 0; i < params.length; i++) {
            this.methodParameters[i] = new Param(httpMethod, method, params[i], paramsAnnos[i]);
        }
    }


    public Result process(String url, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        for (var urlPattern : urlPatterns) {
            Matcher matcher = urlPattern.matcher(url);
            if (matcher.matches()) {
                Object[] args = new Object[methodParameters.length];
                for (int i = 0; i < args.length; i++) {
                    Param param = methodParameters[i];
                    args[i] = switch (param.getParamType()) {
                        case PATH_VARIABLE -> {
                            String s = matcher.group(param.getName());
                            yield convert(param.getClassType(), s);
                        }
                        case REQUEST_BODY -> {
                            BufferedReader reader = req.getReader();
                            yield JsonUtil.readJson(reader, param.getClassType());
                        }
                        case REQUEST_PARAM -> {
                            String s = getOrDefault(req, param.getName(), param.getDefaultValue());
                            yield convert(param.getClassType(), s);
                        }
                        case SERVLET_VARIABLE -> {
                            Class<?> classType = param.getClassType();
                            if (classType == HttpServletRequest.class) {
                                yield req;
                            } else if (classType == HttpServletResponse.class) {
                                yield resp;
                            } else if (classType == HttpSession.class) {
                                yield req.getSession();
                            } else if (classType == ServletContext.class) {
                                yield req.getServletContext();
                            } else {
                                throw new WebException("could not determine argument type: " + classType);
                            }
                        }
                    };
                }
                Object result = ObjectUtil.invokeMethod(controller, handlerMethod, args);
                return new Result(true, result);
            }
        }
        return NOT_PROCESSED;
    }

    private Object convert(Class<?> classType, String s) {
        if (classType == String.class) {
            return s;
        } else if (classType == boolean.class || classType == Boolean.class) {
            return Boolean.valueOf(s);
        } else if (classType == int.class || classType == Integer.class) {
            return Integer.valueOf(s);
        } else if (classType == long.class || classType == Long.class) {
            return Long.valueOf(s);
        } else if (classType == byte.class || classType == Byte.class) {
            return Byte.valueOf(s);
        } else if (classType == short.class || classType == Short.class) {
            return Short.valueOf(s);
        } else if (classType == float.class || classType == Float.class) {
            return Float.valueOf(s);
        } else if (classType == double.class || classType == Double.class) {
            return Double.valueOf(s);
        } else {
            throw new WebException("could not determine argument type: " + classType);
        }
    }

    private String getOrDefault(HttpServletRequest req, String name, String defaultValue) {
        String s = req.getParameter(name);
        if (s == null) {
            if (WebUtil.DEFAULT_PARAM_VALUE.equals(defaultValue)) {
                throw new WebException("request parameter not found: " + name);
            }
            return defaultValue;
        }
        return s;
    }

    public boolean isReturnVoid() {
        return returnVoid;
    }

    public void setReturnVoid(boolean returnVoid) {
        this.returnVoid = returnVoid;
    }

    public Pattern[] getUrlPatterns() {
        return urlPatterns;
    }

    public void setUrlPattern(Pattern[] urlPatterns) {
        this.urlPatterns = urlPatterns;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getHandlerMethod() {
        return handlerMethod;
    }

    public void setHandlerMethod(Method handlerMethod) {
        this.handlerMethod = handlerMethod;
    }

    public Param[] getMethodParameters() {
        return methodParameters;
    }

    public void setMethodParameters(Param[] methodParameters) {
        this.methodParameters = methodParameters;
    }
}
