package com.qins.net.node.annotation;

import com.qins.net.meta.annotation.MethodMapping;
import com.qins.net.node.core.Node;
import com.qins.net.node.http.sender.HttpGetRequest;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@MethodMapping("")
public @interface GetMapping {
    String value();
    int timeout() default -1;
    Class<? extends Node> node() default HttpGetRequest.class;
}
