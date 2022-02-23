package com.qins.net.core.aop.annotation;

import lombok.NonNull;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeEvent {
    String function() default "";
}

