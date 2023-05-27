package com.limyel.tea.web;

import com.limyel.tea.core.io.PropertyResolver;
import com.limyel.tea.ioc.bean.container.BeanContainer;
import com.limyel.tea.ioc.util.BeanContainerUtil;
import com.limyel.tea.web.annotation.Controller;
import com.limyel.tea.web.annotation.GET;
import com.limyel.tea.web.annotation.POST;
import com.limyel.tea.web.annotation.RestController;
import com.limyel.tea.web.exception.WebException;
import com.limyel.tea.web.util.JsonUtil;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class DispatcherServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private BeanContainer beanContainer;
    private PropertyResolver propertyResolver;
    private ViewResolver viewResolver;

    private String resourcePath;
    private String faviconPath;

    List<Dispatcher> getDispatchers = new ArrayList<>();
    List<Dispatcher> postDispatchers = new ArrayList<>();

    public DispatcherServlet(BeanContainer beanContainer) {
        this.beanContainer = beanContainer;
        this.propertyResolver = BeanContainerUtil.getPropertyResolver();
        this.viewResolver = beanContainer.getBean(ViewResolver.class);
        this.resourcePath = propertyResolver.getProperty("${tea.web.static-path:/static/}");
        this.faviconPath = propertyResolver.getProperty("${tea.web.favicon-path:/favicon.ico}}");
        if (!this.resourcePath.endsWith("/")) {
            this.resourcePath += "/";
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        logger.info("init {}", getClass().getName());
        // 查找 @Controller 和 @RestController
        for (var beanDef : beanContainer.findBeanDefines(Object.class)) {
            Class<?> type = beanDef.getType();
            Object instance = beanDef.getRequiredInstance();
            Controller controller = type.getAnnotation(Controller.class);
            RestController restController = type.getAnnotation(RestController.class);
            if (controller != null && restController != null) {
                throw new WebException("found @Controller and @RestController on class: " + type.getName());
            }
            if (controller != null) {
                addController(false, beanDef.getName(), instance);
            }
            if (restController != null) {
                addController(true, beanDef.getName(), instance);
            }
        }
    }

    @Override
    public void destroy() {
        beanContainer.close();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getRequestURI();
        if (url.equals(this.faviconPath) || url.equals(this.resourcePath)) {
            doResource(url, req, resp);
        } else {
            doService(req, resp, this.getDispatchers);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doService(req, resp, this.postDispatchers);
    }

    private void doService(HttpServletRequest req, HttpServletResponse resp, List<Dispatcher> dispatchers) throws ServletException, IOException {
        String url = req.getRequestURI();
        try {
            doService(url, req, resp, dispatchers);
        } catch (IOException | ServletException e) {
            throw e;
        }
    }

    private void doService(String url, HttpServletRequest req, HttpServletResponse resp, List<Dispatcher> dispatchers) throws IOException, ServletException {
        for (var dispatcher : dispatchers) {
            Result result = dispatcher.process(url, req, resp);
            if (result.processed()) {
                Object r = result.returnObject();
                if (dispatcher.isRest()) {
                    // 响应是否已提交
                    if (!resp.isCommitted()) {
                        resp.setContentType("application/json");
                    }
                    if (dispatcher.isResponseBody()) {
                        if (r instanceof String s) {
                            PrintWriter pw = resp.getWriter();
                            pw.write(s);
                            pw.flush();
                        } else if (r instanceof byte[] data) {
                            ServletOutputStream os = resp.getOutputStream();
                            os.write(data);
                            os.flush();
                        } else {
                            throw new WebException("unable to process REST result when handle url: " + url);
                        }
                    } else if (!dispatcher.isReturnVoid()) {
                        PrintWriter pw = resp.getWriter();
                        JsonUtil.writeJson(pw, r);
                    }
                } else {
                    if (!resp.isCommitted()) {
                        resp.setContentType("text/html");
                    }
                    if (r instanceof String s) {
                        if (dispatcher.isResponseBody()) {
                            PrintWriter pw = resp.getWriter();
                            pw.write(s);
                            pw.flush();
                        } else if (s.startsWith("redirect:")) {
                            resp.sendRedirect(s.substring(9));
                        } else {
                            throw new WebException("unable to process String result when handle url: " + url);
                        }
                    } else if (r instanceof byte[] data) {
                        if (dispatcher.isResponseBody()) {
                            ServletOutputStream os = resp.getOutputStream();
                            os.write(data);
                            os.flush();
                        } else {
                            throw new WebException("unable to process byte[] result when handle url: " + url);
                        }
                    } else if (r instanceof ModelAndView mv) {
                        String view = mv.getView();
                        if (view.startsWith("redirect:")) {
                            resp.sendRedirect(view.substring(9));
                        } else {
                            this.viewResolver.render(view, mv.getModel(), req, resp);
                        }
                    } else if (!dispatcher.isReturnVoid() && r != null) {
                        throw new ServletException("unable to process " + r.getClass().getName() + " result when handle url: " + url);
                    }
                }
                return;
            }
        }
        resp.sendError(404, "Not Found");
    }

    private void doResource(String url, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ServletContext ctx = req.getServletContext();
        try (InputStream is = ctx.getResourceAsStream(url)) {
            if (is == null) {
                resp.sendError(404, "Not Found");
            } else {
                String file = url;
                int n = url.lastIndexOf('/');
                if (n >= 0) {
                    file = url.substring(n + 1);
                }
                String mime = ctx.getMimeType(file);
                if (mime == null) {
                    mime = "application/octet-stream";
                }
                resp.setContentType(mime);
                ServletOutputStream os = resp.getOutputStream();
                is.transferTo(os);
                os.flush();
            }
        }
    }

    private void addController(boolean rest, String name, Object instance) {
        addMethods(rest, name, instance, instance.getClass());
    }

    private void addMethods(boolean rest, String name, Object instance, Class<?> type) {
        for (var method : type.getDeclaredMethods()) {
            GET get = method.getAnnotation(GET.class);
            if (get != null) {
                checkMethod(method);
                this.getDispatchers.add(new Dispatcher("GET", rest, instance, method, get.value()));
            }
            POST post = method.getAnnotation(POST.class);
            if (post != null) {
                checkMethod(method);
                this.postDispatchers.add(new Dispatcher("POST", rest, instance, method, post.value()));
            }
        }
        Class<?> superClass = type.getSuperclass();
        if (superClass != null) {
            addMethods(rest, name, instance, superClass);
        }
    }

    private void checkMethod(Method method) {
        int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            throw new WebException("can't do URL mapping to static method: " + method);
        }
        method.setAccessible(true);
    }
}