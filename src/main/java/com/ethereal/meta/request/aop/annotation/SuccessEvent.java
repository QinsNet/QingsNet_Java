package com.ethereal.meta.request.aop.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SuccessEvent {
    String function();
}
