package com.qins.net.core.aop.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionEvent {
    String function();
    boolean isThrow() default true;
}

