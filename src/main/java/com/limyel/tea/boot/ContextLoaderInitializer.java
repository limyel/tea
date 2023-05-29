package com.limyel.tea.boot;

import com.limyel.tea.core.io.PropertyResolver;
import com.limyel.tea.ioc.bean.container.BeanContainer;
import com.limyel.tea.ioc.bean.container.DefaultBeanContainer;
import com.limyel.tea.ioc.util.BeanContainerUtil;
import com.limyel.tea.web.DispatcherServlet;
import com.limyel.tea.web.util.WebUtil;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;

import java.util.Set;

public class ContextLoaderInitializer implements ServletContainerInitializer {

    private PropertyResolver propertyResolver;

    public ContextLoaderInitializer(Class<?> configClass) {
        DefaultBeanContainer.createInstance(configClass);
    }

    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        propertyResolver = BeanContainerUtil.getRequiredPropertyResolver();

        String encoding = propertyResolver.getProperty("${tea.web.character-encoding:UTF-8}");
        servletContext.setRequestCharacterEncoding(encoding);
        servletContext.setResponseCharacterEncoding(encoding);

        registerDispatcherServlet(servletContext);
    }

    private void registerDispatcherServlet(ServletContext servletContext) {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        ServletRegistration.Dynamic servlet = servletContext.addServlet("dispatcherServlet", dispatcherServlet);
        servlet.addMapping("/");
        servlet.setLoadOnStartup(0);
    }
}
