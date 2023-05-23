package com.limyel.tea.ioc.bean.container;

public interface BeanContainer {

    boolean containsBean(String name);

    Object getBean(String name);

    <T> T getBean(Class<T> type);

    <T> T getBean(String name, Class<T> type);

    <T> T getBeans(Class<T> type);

    void close();

}
