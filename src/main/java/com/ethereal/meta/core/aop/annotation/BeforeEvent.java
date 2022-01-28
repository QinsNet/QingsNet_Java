package com.ethereal.meta.core.aop.annotation;

import lombok.NonNull;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeEvent {
    String function() default "";
}

