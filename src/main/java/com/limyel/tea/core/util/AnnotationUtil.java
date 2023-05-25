package com.limyel.tea.core.util;

import com.limyel.tea.core.exception.TeaException;
import com.limyel.tea.ioc.annotation.Config;
import com.limyel.tea.ioc.annotation.Order;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class AnnotationUtil {

    public static List<Method> findMethods(Class<?> type, Class<? extends Annotation> annoClass) {
        List<Method> methods = Arrays.stream(type.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(annoClass))
                .toList();
        if (methods.isEmpty()) {
            return null;
        }
        return methods;
    }

    public static <A extends Annotation> A findAnnotation(Class<?> type, Class<A> annoClass) {
        A annotation = type.getAnnotation(annoClass);
        for (Annotation anno : type.getAnnotations()) {
            Class<? extends Annotation> annoType = anno.annotationType();
            // 元注解，跳过
            if (!annoType.getPackageName().equals("java.lang.annotation")) {
                A found = findAnnotation(annoType, annoClass);
                if (found != null) {
                    if (annotation != null) {
                        throw new TeaException("在 " + type.getSimpleName() + " 中重复使用注解 @"
                                + annoClass.getSimpleName());
                    }
                    annotation = found;
                }
            }
        }
        return annotation;
    }

    public static <A extends Annotation> A getAnnotation(Annotation[] annotations, Class<A> annoClass) {
        for (Annotation annotation : annotations) {
            if (annoClass.isInstance(annotation)) {
                return (A) annotation;
            }
        }
        return null;
    }

    public static boolean isConfig(Class<?> type) {
        Config annotation = findAnnotation(type, Config.class);
        return annotation != null;
    }

}
