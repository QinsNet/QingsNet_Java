package com.qins.net.node.annotation;

import com.qins.net.meta.annotation.method.MethodMapping;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@MethodMapping
@Repeatable(NodeMappings.class)
public @interface NodeMapping {
    String name();
    String host();
}
