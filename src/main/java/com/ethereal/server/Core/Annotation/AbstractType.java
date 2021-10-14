package com.ethereal.server.Core.Annotation;

import java.lang.annotation.*;

@Documented
@Target(value = {ElementType.PARAMETER,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AbstractType {
        String abstractName() default "";
}
