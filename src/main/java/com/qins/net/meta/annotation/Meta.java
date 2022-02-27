package com.qins.net.meta.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE,ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Meta {
    String value() default "";
    String[] nodes() default {};
}
