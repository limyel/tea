package com.limyel.tea.ioc.bean.container;

import com.limyel.tea.ioc.bean.BeanDefineRegistry;

import java.util.List;

/**
 * Bean 容器的接口
 */
public interface BeanContainer extends BeanDefineRegistry {

    boolean containsBean(String name);

    Object getBean(String name);

    <T> T getBean(Class<T> type);

    <T> T getBean(String name, Class<T> type);

    <T> List<T> getBeans(Class<T> type);

    void close();

}
