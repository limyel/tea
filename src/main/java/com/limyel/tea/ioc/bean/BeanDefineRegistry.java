package com.limyel.tea.ioc.bean;

import java.util.List;

/**
 * BeanDefine 仓库接口
 */
public interface BeanDefineRegistry {

    BeanDefine findBeanDefine(String name);

    BeanDefine findBeanDefine(String name, Class<?> type);

    BeanDefine findBeanDefine(Class<?> type);

    List<BeanDefine> findBeanDefines(Class<?> type);

    void registerBeanDefine(BeanDefine beanDefine);

}
