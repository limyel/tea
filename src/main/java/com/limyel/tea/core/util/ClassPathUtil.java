package com.limyel.tea.core.util;

import com.limyel.tea.core.exception.TeaException;
import com.limyel.tea.core.io.InputStreamCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 读取类路径的资源
 */
public class ClassPathUtil {

    private static Logger logger = LoggerFactory.getLogger(ClassPathUtil.class);

    private static String TMP_STATIC_DIR = "/tmp/tea/static";

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

    public static String getProjectBasePath(Class<?> clazz) {
        String projectPath = getProjectPath(clazz);
        String basePath = clazz.getResource("").getPath();
        logger.info(basePath);
        if (isJar(clazz)) {
            basePath = TMP_STATIC_DIR;
        } else {
            basePath = basePath.substring(0, basePath.indexOf("target")) + "target/classes/static";
        }
        File file = new File(basePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return basePath;
    }

    public static String getProjectPath(Class<?> clazz) {
        String basePath = clazz.getResource("").getPath();
        if (basePath.indexOf("classes") > 0) {
            return basePath.substring(0, basePath.indexOf("classes") + 7);
        } else {
            return basePath.substring(0, basePath.indexOf("!") + 1);
        }
    }

    public static boolean isJar(Class<?> clazz) {
        String path = clazz.getResource("").getPath();
        return !path.contains("classes");
    }

    public static File createTmpDir(String name) {
        try {
            File tmpDir = Files.createTempDirectory(name).toFile();
            tmpDir.deleteOnExit();
            return tmpDir;
        } catch (IOException e) {
            throw new TeaException(e);
        }
    }

}
