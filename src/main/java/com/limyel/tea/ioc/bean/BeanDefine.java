package com.limyel.tea.ioc.bean;

import com.limyel.tea.core.exception.TeaException;
import com.limyel.tea.core.util.AnnotationUtil;
import com.limyel.tea.core.util.ClassUtil;
import com.limyel.tea.core.util.ObjectUtil;
import com.limyel.tea.ioc.annotation.*;
import com.limyel.tea.ioc.exception.BeanDefineException;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

public class BeanDefine implements Comparable<BeanDefine> {

    // 名称
    private String name;
    // 类型
    private Class<?> type;
    // 实例
    private Object instance;
    // 构造器
    private Constructor<?> constructor;
    // 工厂名称
    private String factoryName;
    // 工厂方法
    private Method factoryMethod;
    // 顺序
    private int order;
    // 是否为 Primary
    private boolean primary;
    // 是否为单例
    private boolean singleton;

    private String initMethodName;
    private String destroyMethodName;
    private Method initMethod;
    private Method destroyMethod;

    /**
     * 用于生成 @Component 注解的 Bean 的定义
     *
     * @param type
     */
    public BeanDefine(Class<?> type) {
        this.name = getBeanName(type);
        this.type = type;
        this.constructor = ClassUtil.getSuitableConstructor(type);
        this.order = getOrder(type);
        this.primary = type.isAnnotationPresent(Primary.class);
        this.initMethod = findInitDestroyMethod(type, InitMethod.class);
        this.destroyMethod = findInitDestroyMethod(type, DestroyMethod.class);
    }

    /**
     * 用于生成 @Bean 注解的 Bean 的定义
     * @param type
     * @param factoryName
     * @param factoryMethod
     * @param bean
     */
    public BeanDefine(Class<?> type, String factoryName, Method factoryMethod, Bean bean) {
        this.name = getBeanName(factoryMethod);
        this.type = type;
        this.factoryName = factoryName;
        this.factoryMethod = factoryMethod;
        this.order = getOrder(factoryMethod);
        this.primary = factoryMethod.isAnnotationPresent(Primary.class);
        this.initMethodName = bean.initMethod().isBlank() ? null : bean.initMethod();
        this.destroyMethodName = bean.destroyMethod().isBlank() ? null : bean.initMethod();
    }

    public Object getRequiredInstance() {
        if (this.instance == null) {
            throw new BeanDefineException(String.format("bean define %s has not instance", name));
        }
        return this.instance;
    }

    public boolean isBeanPostProcessorDefinition() {
        return BeanPostProcessor.class.isAssignableFrom(this.type);
    }

    private int getOrder(AnnotatedElement obj) {
        Order order = obj.getAnnotation(Order.class);
        return order == null ? Integer.MAX_VALUE : order.value();
    }

    public static String getBeanName(Method method) {
        Bean bean = method.getAnnotation(Bean.class);
        String name = bean.value();
        if (name.isBlank()) {
            return method.getName();
        }
        return name;
    }

    public static String getBeanName(Class<?> type) {
        String name = "";
        Component component = type.getAnnotation(Component.class);
        if (component != null) {
            name = component.value();
        } else {
            for (Annotation annotation : type.getAnnotations()) {
                if (AnnotationUtil.findAnnotation(annotation.annotationType(), Component.class) != null) {
                    name = (String) ObjectUtil.invokeMethod(annotation, "value");
                }
            }
        }
        if (name.isBlank()) {
            name = type.getSimpleName();
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }

    private Method findInitDestroyMethod(Class<?> type, Class<? extends Annotation> annoType) {
        List<Method> methods = AnnotationUtil.findMethods(type, annoType);
        if (methods == null) {
            return null;
        }
        if (methods.size() > 1) {
            throw new TeaException(String.format("class %s has more than one init/destroy method", type.getName()));
        }
        return methods.get(0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    public String getFactoryName() {
        return factoryName;
    }

    public void setFactoryName(String factoryName) {
        this.factoryName = factoryName;
    }

    public Method getFactoryMethod() {
        return factoryMethod;
    }

    public void setFactoryMethod(Method factoryMethod) {
        this.factoryMethod = factoryMethod;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public String getInitMethodName() {
        return initMethodName;
    }

    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    public String getDestroyMethodName() {
        return destroyMethodName;
    }

    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }

    public Method getInitMethod() {
        return initMethod;
    }

    public void setInitMethod(Method initMethod) {
        this.initMethod = initMethod;
    }

    public Method getDestroyMethod() {
        return destroyMethod;
    }

    public void setDestroyMethod(Method destroyMethod) {
        this.destroyMethod = destroyMethod;
    }

    @Override
    public int compareTo(BeanDefine beanDefine) {
        int compare = Integer.compare(this.order, beanDefine.order);
        if (compare != 0) {
            return compare;
        }
        return this.name.compareTo(beanDefine.name);
    }
}
