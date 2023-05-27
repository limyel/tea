package com.limyel.tea.ioc.util;

import com.limyel.tea.core.io.PropertyResolver;
import com.limyel.tea.ioc.bean.container.BeanContainer;

import java.util.Objects;

/**
 * BeanContainer 工具类
 */
public class BeanContainerUtil {

    private static BeanContainer beanContainer = null;

    private static PropertyResolver propertyResolver = null;

    public static BeanContainer getRequiredBeanContainer() {
        return Objects.requireNonNull(getBeanContainer(), "bean container not found");
    }

    public static PropertyResolver getRequiredPropertyResolver() {
        return Objects.requireNonNull(getPropertyResolver(), "property resolver not found");
    }

    public static BeanContainer getBeanContainer() {
        return beanContainer;
    }

    public static void setBeanContainer(BeanContainer beanContainer) {
        BeanContainerUtil.beanContainer = beanContainer;
    }

    public static PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

    public static void setPropertyResolver(PropertyResolver propertyResolver) {
        BeanContainerUtil.propertyResolver = propertyResolver;
    }
}
