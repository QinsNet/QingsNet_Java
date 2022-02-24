package com.qins.net.meta.core;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

@Getter
@Setter
public abstract class MetaNodeParameter extends MetaParameter{
    protected Class<?> proxyClass;
    public MetaNodeParameter(Parameter parameter) {
        super(parameter);
    }
}
