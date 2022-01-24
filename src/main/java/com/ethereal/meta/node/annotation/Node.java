package com.ethereal.meta.node.annotation;

import com.ethereal.meta.core.annotation.BaseParam;

import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@BaseParam
public @interface Node {
        String[] parameters() default {};
}
