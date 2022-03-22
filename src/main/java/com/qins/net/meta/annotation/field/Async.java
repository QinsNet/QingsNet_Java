package com.qins.net.meta.annotation.field;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE,ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Field
public @interface Async {
    String value() default "";
    String[] nodes() default {};
}
