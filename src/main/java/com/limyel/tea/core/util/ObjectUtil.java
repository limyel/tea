package com.limyel.tea.core.util;

import com.limyel.tea.core.exception.TeaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ObjectUtil {

    private static Logger logger = LoggerFactory.getLogger(ObjectUtil.class);

    public static Object newInstance(Class<?> clazz) {
        Object instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.atDebug().log("new instance failed: {}", clazz.getName());
            throw new TeaException(e);
        }
        return instance;
    }

    public static Object invokeMethod(Object obj, Method method, Object... args) {
        Object result;
        try {
            method.setAccessible(true);
            result = method.invoke(obj, args);
        } catch (Exception e) {
            logger.atDebug().log("invoke method failed: {}.{}", obj.getClass().getName(), method.getName());
            throw new TeaException(e);
        }
        return result;
    }

    public static Object invokeMethod(Object obj, String methodName, Object... args) {
        Method method;
        try {
            if (obj instanceof Annotation) {
                method = ((Annotation) obj).annotationType().getDeclaredMethod(methodName);
            } else {
                method = obj.getClass().getDeclaredMethod(methodName);
            }
            return invokeMethod(obj, method, args);
        } catch (Exception e) {
            logger.atError().log("invoke method failed: {}.{}", obj.getClass().getName(), methodName);
            throw new TeaException(e);
        }
    }

    public static void setField(Object obj, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            logger.atDebug().log("set field failed: {}.{}", obj.getClass().getName(), field.getName());
            throw new TeaException(e);
        }
    }

}
