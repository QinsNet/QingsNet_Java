package com.ethereal.meta.core.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(value = {ElementType.METHOD,ElementType.PARAMETER,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseParam {

}
