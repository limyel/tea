package com.limyel.tea.aop.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Around {

    // InvocationHandler Bean 的名称
    String value();

}
