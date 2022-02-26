package com.qins.net.node.annotation;

import com.qins.net.meta.annotation.MethodMapping;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@MethodMapping
public @interface NodeMappings {
    NodeMapping[] value();
}
