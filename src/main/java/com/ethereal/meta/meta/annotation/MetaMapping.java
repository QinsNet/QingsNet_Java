package com.ethereal.meta.meta.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaMapping {
    String mapping();
}
