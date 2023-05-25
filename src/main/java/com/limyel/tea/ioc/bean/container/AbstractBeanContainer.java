package com.limyel.tea.ioc.bean.container;

import com.limyel.tea.core.io.PropertyResolver;
import com.limyel.tea.core.io.ResourceResolver;
import com.limyel.tea.core.util.AnnotationUtil;
import com.limyel.tea.ioc.annotation.*;
import com.limyel.tea.ioc.bean.BeanDefine;
import com.limyel.tea.ioc.bean.DefaultBeanDefineRegistry;
import com.limyel.tea.ioc.exception.BeanContainerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 负责生成 BeanDefine，初始化 PropertyResolversra
 */
public abstract class AbstractBeanContainer extends DefaultBeanDefineRegistry implements BeanContainer {

    private Logger logger = LoggerFactory.getLogger(getClass());

    protected PropertyResolver propertyResolver;

    public AbstractBeanContainer(Class<?> configType) {
        super();
        final Set<String> beanClassNames = scanForClassNames(configType);
        createBeanDefines(beanClassNames);

        propertyResolver = PropertyResolver.of();

    }

    /**
     * 扫描获取所有 Bean 的 Class
     *
     * @param configType
     * @return
     */
    protected Set<String> scanForClassNames(Class<?> configType) {
        // 获取扫描的包路径，如果没有，则以配置类所在包开始
        ComponentScan componentScan = AnnotationUtil.findAnnotation(configType, ComponentScan.class);
        final String[] scanPackages = (componentScan == null) || (componentScan.value().length == 0) ?
                new String[]{configType.getPackageName()} : componentScan.value();

        Set<String> result = new HashSet<>();
        for (String packageName : scanPackages) {
            logger.atDebug().log("scan package: {}", packageName);
            ResourceResolver resourceResolver = new ResourceResolver(packageName);
            List<String> classNameList = resourceResolver.scan(resource -> {
                String name = resource.name();
                if (name.endsWith(".class")) {
                    return name.substring(0, name.length() - 6).replace("/", ".")
                            .replace("\\", ".");
                }
                return null;
            });
            result.addAll(classNameList);
        }

        Import importAnno = configType.getAnnotation(Import.class);
        if (importAnno != null) {
            for (Class<?> importType : importAnno.value()) {
                String importTypeName = importType.getName();
                if (result.contains(importTypeName)) {
                    logger.warn("bean class {} has be existed", importTypeName);
                } else {
                    result.add(importTypeName);
                }
            }
        }

        return result;
    }

    /**
     * 创建所有 BeanDefine
     *
     * @param classNames
     */
    protected void createBeanDefines(Set<String> classNames) {
        for (var className : classNames) {
            Class<?> type = null;
            try {
                type = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new BeanContainerException("class " + className + " not found", e);
            }
            if (type.isAnnotation() || type.isEnum() || type.isRecord() || type.isInterface()) {
                continue;
            }

            Component component = AnnotationUtil.findAnnotation(type, Component.class);
            if (component != null) {
                int modifiers = type.getModifiers();
                if (Modifier.isAbstract(modifiers)) {
                    throw new BeanContainerException("@Component can't be used in abstract class: " + className);
                }
                if (Modifier.isPrivate(modifiers)) {
                    throw new BeanContainerException("@Component can't be used in private class: " + className);
                }
                BeanDefine beanDefine = new BeanDefine(type);
                registerBeanDefine(beanDefine);

                Config config = AnnotationUtil.findAnnotation(type, Config.class);
                if (config != null) {
                    scanFactoryMethods(type);
                }
            }
        }
    }

    private void scanFactoryMethods(Class<?> factoryClass) {
        String factoryName = BeanDefine.getBeanName(factoryClass);
        for (var method : factoryClass.getDeclaredMethods()) {
            Bean bean = method.getAnnotation(Bean.class);
            if (bean != null) {
                int modifiers = method.getModifiers();
                if (Modifier.isPrivate(modifiers)) {
                    throw new BeanContainerException(String.format("@Bean can't be used in private method: %s.%s", factoryName, method.getName()));
                }
                if (Modifier.isFinal(modifiers)) {
                    throw new BeanContainerException(String.format("@Bean can't be used in final method: %s.%s", factoryName, method.getName()));
                }
                if (Modifier.isAbstract(modifiers)) {
                    throw new BeanContainerException(String.format("@Bean can't be used in abstract method: %s.%s", factoryName, method.getName()));
                }
                Class<?> beanType = method.getReturnType();
                if (beanType.isPrimitive()) {
                    throw new BeanContainerException(String.format("@Bean method's return type can't be primitive: %s.%s", factoryName, method.getName()));
                }
                if (beanType == void.class || beanType == Void.class) {
                    throw new BeanContainerException(String.format("@Bean method's return type can't be void: %s.%s", factoryName, method.getName()));
                }
                BeanDefine beanDefine = new BeanDefine(beanType, BeanDefine.getBeanName(factoryClass), method, bean);

                registerBeanDefine(beanDefine);
            }
        }
    }


}
