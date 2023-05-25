package com.limyel.tea.core.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 资源解析器，用于扫描 Class
 */
public class ResourceResolver {

    Logger logger = LoggerFactory.getLogger(getClass());

    private String basePackage;

    public ResourceResolver(String basePackage) {
        this.basePackage = basePackage;
    }

    public ClassLoader getClassLoader() {
        ClassLoader classLoader = null;
        classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        return classLoader;
    }

    /**
     * 扫描 Resource
     *
     * @param mapper
     * @param <T>
     * @return
     */
    public <T> List<T> scan(Function<Resource, T> mapper) {
        // 包路径转为文件路径
        String basePackagePath = this.basePackage.replace(".", "/");
        try {
            List<T> result = new ArrayList<>();
            doScan(basePackagePath, result, mapper);
            return result;
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行资源扫描
     *
     * @param basePackagePath 扫描的文件路径
     * @param result 接受结果
     * @param mapper 筛选函数
     * @param <T>
     * @throws IOException
     * @throws URISyntaxException
     */
    private <T> void doScan(String basePackagePath, List<T> result, Function<Resource, T> mapper) throws IOException, URISyntaxException {
        logger.atDebug().log("scan path: {}", basePackagePath);
        Enumeration<URL> enumeration = getClassLoader().getResources(basePackagePath);
        while (enumeration.hasMoreElements()) {
            URL url = enumeration.nextElement();
            // 定位资源
            URI uri = url.toURI();
            String uriStr = removeTrailingSlash(uri.toString());
            // 除去扫描路径的绝对路径
            String uriBaseStr = uriStr.substring(0, uriStr.length() - basePackagePath.length());
            if (uriBaseStr.startsWith("file:")) {
                uriBaseStr = uriBaseStr.substring(5);
            }
            if (uriBaseStr.startsWith("jar:")) {
                scanFile(true, uriBaseStr, jarUriToPath(basePackagePath, uri), result, mapper);
            } else {
                scanFile(false, uriBaseStr, Paths.get(uri), result, mapper);
            }
        }
    }

    private Path jarUriToPath(String basePackagePath, URI jarUri) throws IOException {
        return FileSystems.newFileSystem(jarUri, Map.of()).getPath(basePackagePath);
    }

    /**
     * 扫描文件
     *
     * @param isJar 是否为 jar 包
     * @param base 除去扫描路径的绝对路径
     * @param root
     * @param collector
     * @param mapper
     * @param <R>
     * @throws IOException
     */
    private <R> void scanFile(boolean isJar, String base, Path root, List<R> collector, Function<Resource, R> mapper) throws IOException {
        String baseDir = removeTrailingSlash(base);
        Files.walk(root).filter(Files::isRegularFile).forEach(file -> {
            Resource resource = null;
            if (isJar) {
                resource = new Resource(baseDir, removeLeadingSlash(file.toString()));
            } else {
                String path = file.toString();
                String name = removeLeadingSlash(path.substring(baseDir.length()));
                resource = new Resource("file:" + path, name);
            }
            logger.atDebug().log("resource found: {}", resource);
            R r = mapper.apply(resource);
            if (r != null) {
                collector.add(r);
            }
        });
    }

    private String removeLeadingSlash(String s) {
        if (s.startsWith("/") || s.startsWith("\\")) {
            s = s.substring(1);
        }
        return s;
    }

    private String removeTrailingSlash(String s) {
        if (s.endsWith("/") || s.endsWith("\\")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

}
