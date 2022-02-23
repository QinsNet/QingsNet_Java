package com.qins.net.core.aop.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(value = {ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EventContextParam {

}

