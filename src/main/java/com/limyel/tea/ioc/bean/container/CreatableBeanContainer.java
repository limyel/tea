package com.limyel.tea.ioc.bean.container;

import com.limyel.tea.ioc.bean.BeanDefine;

public interface CreatableBeanContainer extends BeanContainer {

    void createNormalBeans();

    Object createEarlyBean(BeanDefine beanDefine);
}
