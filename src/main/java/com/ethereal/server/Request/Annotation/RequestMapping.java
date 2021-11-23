package com.ethereal.server.Request.Annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
        String mapping();
        int invokeType() default InvokeTypeFlags.Remote;
}
