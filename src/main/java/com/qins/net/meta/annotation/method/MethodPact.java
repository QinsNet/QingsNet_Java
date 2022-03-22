package com.qins.net.meta.annotation.method;

import com.qins.net.node.core.Node;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
public class MethodPact {
        int timeout;
        String name;
        Class<? extends Node> nodeClass;
        Set<String> nodes;
}