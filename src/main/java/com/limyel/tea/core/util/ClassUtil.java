package com.limyel.tea.core.util;

import com.limyel.tea.core.exception.TeaException;

import java.lang.reflect.Constructor;

/**
 * Class 工具类
 */
public class ClassUtil {

    public static Constructor<?> getSuitableConstructor(Class<?> type) {
        Constructor<?>[] cons = type.getConstructors();
        if (cons.length == 0) {
            cons = type.getDeclaredConstructors();
            if (cons.length != 1) {
                throw new TeaException(type.getName() + " has more than one constructor");
            }
        }
        if (cons.length != 1) {
            throw new TeaException(type.getName() + " has more than one constructor");
        }
        return cons[0];
    }

}
