package com.ethereal.server.Request.Event.Annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SuccessEvent {
    String function();
}
