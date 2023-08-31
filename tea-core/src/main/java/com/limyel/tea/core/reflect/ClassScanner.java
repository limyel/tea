package com.limyel.tea.core.reflect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassScanner {

    private static final Logger logger = LoggerFactory.getLogger(ClassScanner.class);

    public static void main(String[] args) {

        Set<Class<?>> classes = scan("com.limyel.tea");
        for (var clazz : classes) {
            logger.debug(clazz.getName());
        }
    }

    private static Set<Class<?>> scan(String packageName) {
        Set<Class<?>> result = new HashSet<>();

        String packagePath = packageName.replace(".", File.separator);
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                String protocol = url.getProtocol();

                if ("file".equals(protocol)) {
                    File dir = new File(url.toURI());
                    scanFile(dir, packageName, result);
                } else if ("jar".equals(protocol)) {
                    JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                    scanJar(jarFile, packagePath, result);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private static void scanFile(File dir, String packageName, Set<Class<?>> result) throws ClassNotFoundException {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] dirFiles = dir.listFiles(path -> path.isDirectory() || path.getName().endsWith(".class"));
        if (dirFiles == null) {
            return;
        }

        for (var file : dirFiles) {
            if (file.isDirectory()) {
                scanFile(file, packageName + "." + file.getName(), result);
                continue;
            }

            String className = file.getName();
            className = className.substring(0, className.length() - 6);

            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className);
            result.add(clazz);
        }
    }

    private static void scanJar(JarFile jarFile, String packagePath, Set<Class<?>> result) throws ClassNotFoundException {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();

            String name = removeLeadingSlash(jarEntry.getName());
            if (jarEntry.isDirectory() || !name.startsWith(packagePath) || !name.endsWith(".class")) {
                continue;
            }

            String className = name.substring(0, name.length() - 6);
            System.out.println(className);
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className.replace(File.separator, "."));
            result.add(clazz);
        }
    }

    private static String removeLeadingSlash(String s) {
        if (s.startsWith("/") || s.startsWith("\\")) {
            s = s.substring(1);
        }
        return s;
    }

    private static String removeTrailingSlash(String s) {
        if (s.endsWith("/") || s.endsWith("\\")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

}
