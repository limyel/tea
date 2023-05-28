package com.limyel.tea.core.util;

import com.limyel.tea.core.io.InputStreamCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 读取类路径的资源
 */
public class ClassPathUtil {

    private static String TMP_PUBLIC_DIR = "/tmp/tea/public";

    public static <T> T readInputStream(String path, InputStreamCallback<T> inputStreamCallback) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        try (InputStream inputStream = getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new FileNotFoundException("file not found in classpath: " + path);
            }
            return inputStreamCallback.doWithInputStream(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readString(String path) {
        return readInputStream(path, is -> {
            byte[] data =is.readAllBytes();
            return new String(data, StandardCharsets.UTF_8);
        });
    }

    public static ClassLoader getClassLoader() {
        ClassLoader classLoader = null;
        classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClassPathUtil.class.getClassLoader();
        }
        return classLoader;
    }

    public static String getPublicPath(Class<?> clazz) {
        String basePath = clazz.getResource("").getPath();
        if (isJar(clazz)) {
            basePath = TMP_PUBLIC_DIR;
        } else {
            basePath = basePath.substring(0, basePath.indexOf("target")) + "target/classes/public";
        }
        File file = new File(basePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return basePath;
    }

    public static boolean isJar(Class<?> clazz) {
        String path = clazz.getResource("").getPath();
        return !path.contains("classes");
    }

}
