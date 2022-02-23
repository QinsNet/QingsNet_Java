package com.qins.net.service.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ServiceAnnotation
public @interface GetService {
    String value();
    int timeout() default -1;
}
