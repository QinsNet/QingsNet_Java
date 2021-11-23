package com.ethereal.server.Core.Manager.Event.Annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionEvent {
    String function();
    boolean isThrow() default true;
}

