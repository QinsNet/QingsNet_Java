package com.ethereal.meta.service.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ServiceAnnotation
public @interface MetaService {
    String value();
    int timeout() default -1;
}
