package com.ethereal.net.node.annotation;

import com.ethereal.net.core.annotation.BaseParam;

import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@BaseParam
public @interface Node {
        String[] parameters() default {};
}
