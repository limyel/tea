package com.limyel.tea.ioc.util;

import com.limyel.tea.ioc.bean.container.BeanContainer;

import java.util.Objects;

public class BeanContainerUtil {

    private static BeanContainer beanContainer = null;

    public static BeanContainer getRequiredBeanContainer() {
        return Objects.requireNonNull(getBeanContainer(), "bean container not found");
    }

    public static BeanContainer getBeanContainer() {
        return beanContainer;
    }

    public static void setBeanContainer(BeanContainer beanContainer) {
        BeanContainerUtil.beanContainer = beanContainer;
    }
}
