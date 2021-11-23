package com.ethereal.server.Service.Annotation;

import com.ethereal.server.Core.Annotation.BaseParam;

import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@BaseParam
public @interface Token {
        String[] parameters() default {};
}
