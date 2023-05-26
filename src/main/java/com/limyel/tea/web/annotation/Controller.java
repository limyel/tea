package com.limyel.tea.web.annotation;

import com.limyel.tea.ioc.annotation.Bean;
import com.limyel.tea.ioc.annotation.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Component
public @interface Controller {

    String value() default "";

}
