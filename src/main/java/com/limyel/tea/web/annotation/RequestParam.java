package com.limyel.tea.web.annotation;

import com.limyel.tea.web.util.WebUtil;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface RequestParam {

    String value();

    String defaultValue() default WebUtil.DEFAULT_PARAM_VALUE;

}
