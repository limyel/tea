package com.limyel.tea.ioc.bean;

import com.limyel.tea.ioc.exception.BeanDefineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultBeanDefineRegistry implements BeanDefineRegistry {

    private Logger logger = LoggerFactory.getLogger(getClass());

    protected Map<String, BeanDefine> beans;

    public DefaultBeanDefineRegistry() {
        beans = new HashMap<>();
    }

    @Override
    public BeanDefine findBeanDefine(String name) {
        return beans.get(name);
    }

    @Override
    public BeanDefine findBeanDefine(String name, Class<?> type) {
        BeanDefine beanDefine = this.findBeanDefine(name);
        if (beanDefine == null) {
            throw new BeanDefineException(String.format("bean define not found: %s", name));
        }
        if (!type.isAssignableFrom(beanDefine.getType())) {
            throw new BeanDefineException(String.format("class %s is not assignable from bean define's type", type.getName()));
        }
        return beanDefine;
    }

    @Override
    public BeanDefine findBeanDefine(Class<?> type) {
        List<BeanDefine> beanDefines = findBeanDefines(type);
        if (beanDefines.isEmpty()) {
            return null;
        }
        if (beanDefines.size() == 1) {
            return beanDefines.get(0);
        }
        List<BeanDefine> primarys = beanDefines.stream().filter(BeanDefine::isPrimary).toList();
        if (primarys.size() == 1) {
            return primarys.get(0);
        }
        if (primarys.size() > 1) {
            throw new BeanDefineException(String.format("more than one primary bean define in class %s", type.getName()));
        } else {
            throw new BeanDefineException(String.format("more than one bean define in class %s, but has no primary", type.getName()));
        }
    }

    @Override
    public List<BeanDefine> findBeanDefines(Class<?> type) {
        return beans.values().stream()
                .filter(beanDefine -> type.isAssignableFrom(beanDefine.getType()))
                .sorted().toList();
    }

    @Override
    public void registerBeanDefine(BeanDefine beanDefine) {
        if (beans.containsKey(beanDefine.getName())) {
            throw new BeanDefineException(String.format("bean define named {} has registered", beanDefine.getName()));
        }
        logger.atDebug().log("register bean define {}", beanDefine.getName());
        beans.put(beanDefine.getName(), beanDefine);
    }


}
