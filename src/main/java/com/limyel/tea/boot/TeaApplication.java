package com.limyel.tea.boot;

import com.limyel.tea.core.exception.TeaException;
import com.limyel.tea.core.io.PropertyResolver;
import com.limyel.tea.core.util.ClassPathUtil;
import com.limyel.tea.ioc.util.BeanContainerUtil;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.util.Set;

public class TeaApplication {

    private final Logger logger = LoggerFactory.getLogger(TeaApplication.class);

    public static void run(Class<?> configClass, String... args) {
        try {
            new TeaApplication().start(configClass, args);
        } catch (LifecycleException e) {
            throw new TeaException(e);
        }
    }

    public void start(Class<?> configClass, String... args) throws LifecycleException {
        printBanner();

        final long startTime = System.currentTimeMillis();
        final int javaVersion = Runtime.version().feature();
        final long pid = ManagementFactory.getRuntimeMXBean().getPid();
        final String pwd = Paths.get("").toAbsolutePath().toString();
        logger.info("starting {} using Java {} with PID {} (started in {})", configClass.getSimpleName(), javaVersion, pid, pwd);

        Server server = startTomcat(configClass);
        server.await();
    }

    protected Server startTomcat(Class<?> configClass) throws LifecycleException {
        ContextLoaderInitializer contextLoaderInitializer = new ContextLoaderInitializer(configClass);
        PropertyResolver propertyResolver = BeanContainerUtil.getRequiredPropertyResolver();

        int port = propertyResolver.getProperty("${server.port:8080}", int.class);
        final String baseDir = propertyResolver.getProperty("${server.base-dir:/tmp/tea/tomcat}");
        File file = new File(baseDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector().setThrowOnFailure(true);
        Context ctx = tomcat.addWebapp("", ClassPathUtil.getPublicPath(configClass));
        WebResourceRoot resourceRoot = new StandardRoot(ctx);
        resourceRoot.addPreResources(new DirResourceSet(resourceRoot, "/WEB-INF/classes", new File(baseDir).getAbsolutePath(), "/"));
        ctx.setResources(resourceRoot);
        ctx.addServletContainerInitializer(contextLoaderInitializer, Set.of());
        tomcat.start();
        logger.info("tomcat start at port {}", port);
        return tomcat.getServer();
    }

    private void printBanner() {
        String banner = ClassPathUtil.readString("/banner.txt");
        banner.lines().forEach(System.out::println);
    }

}
