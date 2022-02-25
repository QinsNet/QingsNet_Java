package com.qins.net.meta.annotation;

import com.qins.net.node.core.Node;
import com.qins.net.node.http.sender.HttpPostRequest;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@MethodMapping
public @interface MethodMapping {
    int timeout() default -1;
    Class<? extends Node> node() default HttpPostRequest.class;
}
