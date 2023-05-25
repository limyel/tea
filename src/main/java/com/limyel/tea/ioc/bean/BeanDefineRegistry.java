package com.limyel.tea.ioc.bean;

import java.util.List;

public interface BeanDefineRegistry {

    BeanDefine findBeanDefine(String name);

    BeanDefine findBeanDefine(String name, Class<?> type);

    BeanDefine findBeanDefine(Class<?> type);

    List<BeanDefine> findBeanDefines(Class<?> type);

    void registerBeanDefine(BeanDefine beanDefine);

}
