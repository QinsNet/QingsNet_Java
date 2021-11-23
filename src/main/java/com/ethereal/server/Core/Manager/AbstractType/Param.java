package com.ethereal.server.Core.Manager.AbstractType;

import java.lang.annotation.*;

@Documented
@Target(value = {ElementType.PARAMETER,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
        String type() default "";
}
