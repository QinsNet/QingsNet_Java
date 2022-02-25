package com.qins.net.request.annotation;

import com.qins.net.node.core.Node;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MethodPact {
        int timeout;
        Class<? extends Node> nodeClass;
}