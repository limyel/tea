package com.limyel.tea.ioc.bean.container;

import com.limyel.tea.core.util.AnnotationUtil;
import com.limyel.tea.core.util.ObjectUtil;
import com.limyel.tea.ioc.annotation.Autowired;
import com.limyel.tea.ioc.annotation.Value;
import com.limyel.tea.ioc.bean.BeanDefine;
import com.limyel.tea.ioc.bean.BeanPostProcessor;
import com.limyel.tea.ioc.exception.BeanContainerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * 负责创建各种 early bean
 */
public abstract class CreatableBeanContainer extends AbstractBeanContainer {

    private Logger logger = LoggerFactory.getLogger(getClass());

    protected List<String> creatingBeanNames;
    protected List<BeanPostProcessor> beanPostProcessors;

    public CreatableBeanContainer(Class<?> configType) {
        super(configType);
        creatingBeanNames = new ArrayList<>();
        beanPostProcessors = new ArrayList<>();

        // 创建 @Config 配置类
        beans.values().stream()
                .filter(beanDefine -> AnnotationUtil.isConfig(beanDefine.getType()))
                .sorted().forEach(this::createEarlyBean);

        // 创建 BeanPostProcessor
        beanPostProcessors = beans.values().stream().filter(BeanDefine::isBeanPostProcessorDefinition).sorted()
                .map(beanDefine -> (BeanPostProcessor) createEarlyBean(beanDefine)).toList();

        // 创建普通 Bean
        createNormalBeans();
    }

    protected void createNormalBeans() {
        List<BeanDefine> beanDefines = beans.values().stream()
                .filter(beanDefine -> beanDefine.getInstance() == null).sorted().toList();
        beanDefines.forEach(beanDefine -> {
            if (beanDefine.getInstance() == null) {
                createEarlyBean(beanDefine);
            }
        });
    }

    protected Object createEarlyBean(BeanDefine beanDefine) {
        logger.atDebug().log("create early bean: {}", beanDefine.getName());

        //在创建早期 Bean 时，一个 Bean 只会被创建一次，如果超过一次则发生循环依赖
        if (!creatingBeanNames.add(beanDefine.getName())) {
            throw new BeanContainerException("circular dependency " + beanDefine.getName());
        }

        boolean createByFactory = beanDefine.getFactoryName() != null;
        Executable createFn = null;
        if (createByFactory) {
            createFn = beanDefine.getFactoryMethod();
        } else {
            createFn = beanDefine.getConstructor();
        }

        Object[] args = getArgs(beanDefine, createFn);
        Object instance = null;

        if (createByFactory) {
            Object factory = getBean(beanDefine.getFactoryName());
            instance = ObjectUtil.invokeMethod(factory, beanDefine.getFactoryMethod(), args);
        } else {
            try {
                instance = beanDefine.getConstructor().newInstance(args);
            } catch (Exception e) {
                throw new BeanContainerException("create early bean failed: " + beanDefine.getName());
            }
        }
        beanDefine.setInstance(instance);

        for (var processor : beanPostProcessors) {
            Object processed = processor.postProcessBeforeInitialization(beanDefine.getInstance(), beanDefine.getName());
            if (processed != null && beanDefine.getInstance() != processed) {
                beanDefine.setInstance(processed);
            }
        }

        return instance;
    }

    private Object[] getArgs(BeanDefine beanDefine, Executable fn) {
        boolean isConfig = AnnotationUtil.isConfig(beanDefine.getType());

        final Parameter[] parameters = fn.getParameters();
        final Annotation[][] parametersAnnos = fn.getParameterAnnotations();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Annotation[] parameterAnnos = parametersAnnos[i];
            Value value = AnnotationUtil.getAnnotation(parameterAnnos, Value.class);
            Autowired autowired = AnnotationUtil.getAnnotation(parameterAnnos, Autowired.class);

            if (isConfig && autowired != null) {
                throw new BeanContainerException("@Config and @Autowired can't be used together");
            }
            if (autowired != null && value != null) {
                throw new BeanContainerException("@Autowired and @Value can't be used together");
            }
            if (autowired == null && value == null) {
                throw new BeanContainerException("@Authwired and @Value must have at least one");
            }

            Class<?> parameterType = parameter.getType();
            if (value != null) {
                args[i] = propertyResolver.getProperty(value.value(), parameterType);
            } else {
                String name = autowired.name();
                boolean required = autowired.value();
                var parameterBeanDef = name.isBlank() ? findBeanDefine(parameterType) : findBeanDefine(name, parameterType);
                if (required && parameterBeanDef == null) {
                    throw new BeanContainerException(String.format("autowired bean define not found: %s.%s",
                            beanDefine.getType().getName(), parameter.getName()));
                }
                if (parameterBeanDef != null) {
                    var parameterInstance = parameterBeanDef.getInstance();
                    if (parameterInstance == null) {
                        parameterInstance = createEarlyBean(parameterBeanDef);
                    }
                    args[i] = parameterInstance;
                } else {
                    args[i] = null;
                }
            }
        }
        return args;
    }

}
