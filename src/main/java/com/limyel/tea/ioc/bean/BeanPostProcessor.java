package com.limyel.tea.ioc.bean;

public interface BeanPostProcessor {

    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

//    default Object postProcessOnSetProperty(Object bean, String beanName) {
//        return bean;
//    }

}
