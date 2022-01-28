package com.ethereal.meta.request.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestAnnotation
public @interface GetRequest {
    String mapping();
    int invoke() default InvokeTypeFlags.Remote;
    int timeout() default -1;
}
