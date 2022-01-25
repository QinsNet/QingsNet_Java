package com.ethereal.meta.net.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Node {
        String[] parameters() default {};
}
