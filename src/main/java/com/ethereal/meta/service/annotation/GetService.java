package com.ethereal.meta.service.annotation;

import com.ethereal.meta.request.annotation.InvokeTypeFlags;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ServiceAnnotation
public @interface GetService {
    String mapping();
    int timeout() default -1;
}
