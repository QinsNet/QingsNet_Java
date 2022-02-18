package com.ethereal.meta.request.annotation;

import com.ethereal.meta.node.core.Node;
import com.ethereal.meta.node.p2p.sender.HttpPostRequest;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestAnnotation
public @interface MetaRequest {
    String value();
    int invoke() default InvokeTypeFlags.Remote | InvokeTypeFlags.ReturnRemote;
    int timeout() default -1;
    Class<? extends Node> node() default HttpPostRequest.class;
}
