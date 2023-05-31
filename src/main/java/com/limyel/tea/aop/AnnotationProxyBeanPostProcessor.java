package com.limyel.tea.aop;

import com.limyel.tea.aop.exception.AopException;
import com.limyel.tea.core.util.ObjectUtil;
import com.limyel.tea.ioc.bean.BeanDefine;
import com.limyel.tea.ioc.bean.BeanPostProcessor;
import com.limyel.tea.ioc.bean.container.BeanContainer;
import com.limyel.tea.ioc.bean.container.CreatableBeanContainer;
import com.limyel.tea.ioc.util.BeanContainerUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class AnnotationProxyBeanPostProcessor <A extends Annotation> implements BeanPostProcessor {

    private Class<A> annoClass;

    public AnnotationProxyBeanPostProcessor() {
        this.annoClass = getParameterizedType();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Class<?> beanType = bean.getClass();
        A anno = beanType.getAnnotation(annoClass);
        if (anno != null) {
            String handlerName = (String) ObjectUtil.invokeMethod(anno, "value");
            return createProxy(beanType, bean, handlerName);
        } else {
            return bean;
        }
    }

    private Object createProxy(Class<?> beanType, Object bean, String handlerName) {
        CreatableBeanContainer beanContainer = (CreatableBeanContainer) BeanContainerUtil.getRequiredBeanContainer();
        BeanDefine handlerDef = beanContainer.findBeanDefine(handlerName);
        if (handlerDef == null) {
            throw new AopException("handler not found: " + handlerName);
        }
        Object handler = handlerDef.getInstance();
        if (handler == null) {
            handler = beanContainer.createEarlyBean(handlerDef);
        }
        if (handler instanceof InvocationHandler invoHandler) {
            return ProxyResolver.getInstance().createProxy(bean, invoHandler);
        } else {
            throw new AopException("handler should instance of InvocationHandler");
        }
    }

    private Class<A> getParameterizedType() {
        Type type = getClass().getGenericSuperclass();
        if (!(type instanceof ParameterizedType)) {
            throw new AopException("class " + getClass().getName() + " does not have parameterized type.");
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type[] types = parameterizedType.getActualTypeArguments();
        if (types.length != 1) {
            throw new AopException("class " + getClass().getName() + " has more than 1 parameterized types.");
        }
        Type result = types[0];
        if (!(result instanceof Class<?>)) {
            throw new AopException("class " + getClass().getName() + " does not have parameterized type of class.");
        }
        return (Class<A>) result;
    }

}
