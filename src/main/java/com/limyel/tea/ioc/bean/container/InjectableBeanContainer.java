package com.limyel.tea.ioc.bean.container;

import com.limyel.tea.core.util.ObjectUtil;
import com.limyel.tea.ioc.annotation.Autowired;
import com.limyel.tea.ioc.annotation.Value;
import com.limyel.tea.ioc.bean.BeanDefine;
import com.limyel.tea.ioc.exception.BeanContainerException;

import java.lang.reflect.*;

/**
 * 负责处理依赖注入
 */
public abstract class InjectableBeanContainer extends CreatableBeanContainer {

    public InjectableBeanContainer(Class<?> configType) {
        super(configType);

        beans.values().forEach(this::injectBean);
    }

    private void injectBean(BeanDefine beanDefine) {
        injectProperties(beanDefine, beanDefine.getType(), beanDefine.getInstance());
    }

    private void injectProperties(BeanDefine beanDefine, Class<?> type, Object instance) {
        for (var field : type.getDeclaredFields()) {
            doInjectProperties(beanDefine, instance, field);
        }
        for (var method : type.getDeclaredMethods()) {
            doInjectProperties(beanDefine, instance, method);
        }

        // 对父类进行依赖注入
        Class<?> superClass = type.getSuperclass();
        if (superClass != null) {
            injectProperties(beanDefine, superClass, instance);
        }
    }

    private void doInjectProperties(BeanDefine beanDefine, Object instance, AccessibleObject accessible) {
        Value value = accessible.getAnnotation(Value.class);
        Autowired autowired = accessible.getAnnotation(Autowired.class);
        if (value == null && autowired == null) {
            return;
        }
        if (value != null && autowired != null) {
            throw new BeanContainerException("@Autowired and @Value can't be used together");
        }

        Field field = null;
        Method method = null;
        if (accessible instanceof Field f) {
            checkFieldOrMethod(f);
            f.setAccessible(true);
            field = f;
        }
        if (accessible instanceof Method m) {
            checkFieldOrMethod(m);
            if (m.getParameterCount() != 1) {
                throw new BeanContainerException("@Autowired method's parameter count must only one");
            }
            m.setAccessible(true);
            method = m;
        }

        String accessibleName = field == null ? method.getName() : field.getName();
        Class<?> accessibleType = field == null ? method.getParameterTypes()[0] : field.getType();

        // @Value 注入
        if (value != null) {
            Object property = propertyResolver.getProperty(value.value(), accessibleType);
            if (field != null) {
                ObjectUtil.setField(instance, field, property);
            }
            if (method != null) {
                ObjectUtil.invokeMethod(instance, method, property);
            }
        }

        // @Autowired 注入
        if (autowired != null) {
            String name = autowired.name();
            boolean required = autowired.value();
            Object depends = name.isBlank() ? getBean(accessibleType) : getBean(name, accessibleType);
            if (required && depends == null) {
                throw new BeanContainerException(String.format("bean not found: %s", accessibleName));
            }
            if (depends != null) {
                if (field != null) {
                    ObjectUtil.setField(instance, field, depends);
                }
                if (method != null) {
                    ObjectUtil.invokeMethod(instance, method, depends);
                }
            }
        }
    }

    private void checkFieldOrMethod(Member member) {
        int modifiers = member.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            throw new BeanContainerException("field/method can't be static");
        }
        if (Modifier.isFinal(modifiers)) {
            throw new BeanContainerException("field/method can't be final");
        }
    }

}
