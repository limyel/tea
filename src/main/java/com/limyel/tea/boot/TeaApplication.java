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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Set;

public class TeaApplication {

    private final Logger logger = LoggerFactory.getLogger(TeaApplication.class);

    public static void run(Class<?> configClass, String... args) {
        try {
            new TeaApplication().start(configClass, args);
        } catch (Exception e) {
            throw new TeaException(e);
        }
    }

    public void start(Class<?> configClass, String... args) throws Exception {
        printBanner();

        final long startTime = System.currentTimeMillis();
        final int javaVersion = Runtime.version().feature();
        final long pid = ManagementFactory.getRuntimeMXBean().getPid();
        final String pwd = Paths.get("").toAbsolutePath().toString();
        logger.info("starting {} using Java {} with PID {} (started in {})", configClass.getSimpleName(), javaVersion, pid, pwd);

        Server server = startTomcat(configClass);
        server.await();
    }

    protected Server startTomcat(Class<?> configClass) throws LifecycleException, MalformedURLException {
        ContextLoaderInitializer contextLoaderInitializer = new ContextLoaderInitializer(configClass);
        PropertyResolver propertyResolver = BeanContainerUtil.getRequiredPropertyResolver();

        int port = propertyResolver.getProperty("${server.port:8080}", int.class);
        String baseDir = propertyResolver.getProperty("${server.base-dir:}");
        if (baseDir.isBlank()) {
            File tmpDir = ClassPathUtil.createTmpDir("tomcat." + port + ".");
            baseDir = tmpDir.getAbsolutePath();
        }
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir);
        tomcat.setPort(port);
        tomcat.getConnector().setThrowOnFailure(true);
        Context ctx = tomcat.addWebapp("", ClassPathUtil.getStaticPath(configClass));
        if (ClassPathUtil.getProjectPath(configClass).indexOf("!") > 0) {
            String jar = "jar:" + ClassPathUtil.getProjectPath(configClass) + "/";
            URL url = new URL(jar);
            ctx.setResources(new StandardRoot(ctx));
            ctx.getResources().createWebResourceSet(WebResourceRoot.ResourceSetType.RESOURCE_JAR, "/", url, "/static");
        }
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
