package com.qins.net.request.annotation;

import com.qins.net.node.core.Node;
import com.qins.net.node.http.sender.HttpGetRequest;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestAnnotation
public @interface GetRequest {
    String value();
    int invoke() default InvokeTypeFlags.Remote | InvokeTypeFlags.ReturnRemote;
    int timeout() default -1;
    Class<? extends Node> node() default HttpGetRequest.class;
}
