package com.limyel.tea.ioc.bean;

/**
 * Bean 后置处理器接口
 */
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
