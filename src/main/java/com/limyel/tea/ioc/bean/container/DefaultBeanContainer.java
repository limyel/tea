package com.limyel.tea.ioc.bean.container;

import com.limyel.tea.core.io.PropertyResolver;
import com.limyel.tea.core.util.ObjectUtil;
import com.limyel.tea.ioc.bean.BeanDefine;
import com.limyel.tea.ioc.exception.BeanDefineException;
import com.limyel.tea.ioc.util.BeanContainerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DefaultBeanContainer extends InjectableBeanContainer {

    public DefaultBeanContainer(Class<?> configType) {
        super(configType);
        BeanContainerUtil.setBeanContainer(this);

        beans.values().forEach(this::initBean);
    }

    private void initBean(BeanDefine beanDefine) {
        if (beanDefine.getInitMethodName() != null) {
            ObjectUtil.invokeMethod(beanDefine.getInstance(), beanDefine.getInitMethodName());
        }
        if (beanDefine.getInitMethod() != null) {
            ObjectUtil.invokeMethod(beanDefine.getInstance(), beanDefine.getFactoryMethod());
        }
    }

    @Override
    public boolean containsBean(String name) {
        return beans.containsKey(name);
    }

    @Override
    public Object getBean(String name) {
        BeanDefine beanDefine = beans.get(name);
        if (beanDefine == null) {
            throw new BeanDefineException(String.format("bean define {} not found", name));
        }
        return beanDefine.getRequiredInstance();
    }

    @Override
    public <T> T getBean(Class<T> type) {
        BeanDefine beanDefine = findBeanDefine(type);
        if (beanDefine == null) {
            return null;
        }
        return (T) beanDefine.getRequiredInstance();
    }

    @Override
    public <T> T getBean(String name, Class<T> type) {
        return (T) findBeanDefine(name, type);
    }

    @Override
    public <T> List<T> getBeans(Class<T> type) {
        List<BeanDefine> beanDefines = findBeanDefines(type);
        if (beanDefines.isEmpty()) {
            return List.of();
        }
        List<T> result = new ArrayList<>(beanDefines.size());
        for (var beanDefine : beanDefines) {
            result.add((T) beanDefine.getRequiredInstance());
        }
        return result;
    }

    @Override
    public void close() {

    }
}
