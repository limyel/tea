package com.limyel.tea.aop;

import com.limyel.tea.core.io.PropertyResolver;
import com.limyel.tea.core.util.ObjectUtil;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyResolver {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ByteBuddy byteBuddy = new ByteBuddy();

    private static ProxyResolver INSTANCE = null;

    private ProxyResolver() {
    }

    public static ProxyResolver getInstance() {
        if (INSTANCE == null) {
            synchronized (PropertyResolver.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ProxyResolver();
                }
            }
        }
        return INSTANCE;
    }

    public <T> T createProxy(T bean, InvocationHandler handler) {
        Class<?> sourceClass = bean.getClass();
        // 动态创建的代理类型
        Class<?> proxyClass = this.byteBuddy
                // 子类用默认无参构造器
                .subclass(sourceClass, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
                // 拦截所有 public 方法
                .method(ElementMatchers.isPublic()).intercept(InvocationHandlerAdapter.of(
                        // 新的拦截器实例
                        (proxy, method, args) -> {
                            // 将方法调用代理至原始 Bean
                            return handler.invoke(bean, method, args);
                        }
                ))
                // 生成字节码
                .make()
                // 加载字节码
                .load(sourceClass.getClassLoader()).getLoaded();
        // 创建代理实例
        Object proxy;
        proxy = ObjectUtil.newInstance(proxyClass);
        return (T) proxy;
    }

}
