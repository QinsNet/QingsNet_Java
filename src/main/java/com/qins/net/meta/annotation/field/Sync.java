package com.qins.net.meta.annotation.field;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Field
public @interface Sync {
    String value() default "";
}
