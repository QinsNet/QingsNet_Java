package com.qins.net.meta.core;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class MetaNodeReturn extends MetaReturn{
    protected Class<?> proxyClass;
    public MetaNodeReturn(Class<?> instanceClass) {
        super(instanceClass);
    }
}
