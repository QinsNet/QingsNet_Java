package com.qins.net.meta.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaMapping {
    String value();
    Class<?> elementClass() default MetaMapping.class;
}
