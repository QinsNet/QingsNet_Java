package com.ethereal.meta.node.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Node {
        String[] parameters() default {};
}
