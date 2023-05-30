package com.limyel.tea.boot;

import com.limyel.tea.core.io.PropertyResolver;
import com.limyel.tea.core.util.ClassPathUtil;
import com.limyel.tea.ioc.bean.container.BeanContainer;
import com.limyel.tea.ioc.bean.container.DefaultBeanContainer;
import com.limyel.tea.ioc.util.BeanContainerUtil;
import com.limyel.tea.web.DispatcherServlet;
import com.limyel.tea.web.FilterRegistrationBean;
import com.limyel.tea.web.exception.WebException;
import jakarta.servlet.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ContextLoaderInitializer implements ServletContainerInitializer {

    private PropertyResolver propertyResolver;
    private Class<?> configClass;

    public ContextLoaderInitializer(Class<?> configClass) {
        this.configClass = configClass;
        this.propertyResolver = PropertyResolver.getInstance();
        ClassPathUtil.setProjectPath(configClass);
    }

    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        WebMvcConfig.setServletContext(servletContext);
        DefaultBeanContainer.createInstance(configClass);

        String encoding = propertyResolver.getProperty("${tea.web.character-encoding:UTF-8}");
        servletContext.setRequestCharacterEncoding(encoding);
        servletContext.setResponseCharacterEncoding(encoding);

        registerDispatcherServlet(servletContext);
        registerFileters(servletContext);
    }

    private void registerDispatcherServlet(ServletContext servletContext) {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        ServletRegistration.Dynamic servlet = servletContext.addServlet("dispatcherServlet", dispatcherServlet);
        servlet.addMapping("/");
        servlet.setLoadOnStartup(0);
    }

    private void registerFileters(ServletContext servletContext) {
        BeanContainer beanContainer = BeanContainerUtil.getRequiredBeanContainer();
        for (var filterBean : beanContainer.getBeans(FilterRegistrationBean.class)) {
            List<String> urlPatterns = filterBean.getUrlPatterns();
            if (urlPatterns == null || urlPatterns.isEmpty()) {
                throw new WebException();
            }
            Filter filter = Objects.requireNonNull(filterBean.getFileter(), "");
            var filterRegistration = servletContext.addFilter(filterBean.getName(), filter);
            filterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, urlPatterns.toArray(String[]::new));
        }
    }
}
