package com.ethereal.meta.request.annotation;

import com.ethereal.meta.node.core.Node;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestMapping {
        RequestType method;
        String mapping;
        int invoke;
        int timeout;
        Class<? extends Node> nodeClass;
}