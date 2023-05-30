package com.limyel.tea.boot.annotation;

import com.limyel.tea.ioc.annotation.Config;
import com.limyel.tea.ioc.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Config
public @interface TeaApplication {

    String value() default "";

}
