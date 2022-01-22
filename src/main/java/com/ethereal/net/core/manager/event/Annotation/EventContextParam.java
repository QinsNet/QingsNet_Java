package com.ethereal.net.core.manager.event.Annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(value = {ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EventContextParam {

}

