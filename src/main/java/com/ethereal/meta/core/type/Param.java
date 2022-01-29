package com.ethereal.meta.core.type;

import java.lang.annotation.*;

@Documented
@Target(value = {ElementType.PARAMETER,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
        String name();
}
