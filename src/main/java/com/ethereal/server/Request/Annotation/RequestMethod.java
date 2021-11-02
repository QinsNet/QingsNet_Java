package com.ethereal.server.Request.Annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMethod {
        String[] parameters() default {};
        int timeout() default -1;
        int invokeType() default InvokeTypeFlags.Remote;
}
