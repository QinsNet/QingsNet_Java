package com.ethereal.server.Core.Annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AbstractType {
        String abstractName() default "";
}
