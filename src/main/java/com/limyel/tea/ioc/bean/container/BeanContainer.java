package com.limyel.tea.ioc.bean.container;

import java.util.List;

public interface BeanContainer {

    boolean containsBean(String name);

    Object getBean(String name);

    <T> T getBean(Class<T> type);

    <T> T getBean(String name, Class<T> type);

    <T> List<T> getBeans(Class<T> type);

    void close();

}