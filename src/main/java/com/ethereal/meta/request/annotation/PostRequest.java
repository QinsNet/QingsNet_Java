package com.ethereal.meta.request.annotation;

import com.ethereal.meta.node.p2p.sender.Sender;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestAnnotation
public @interface PostRequest {
    String value();
    int invoke() default InvokeTypeFlags.Remote;
    int timeout() default -1;
    Class<? extends Sender> node() default Sender.class;
}
