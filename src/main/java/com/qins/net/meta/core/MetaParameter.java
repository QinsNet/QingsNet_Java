package com.qins.net.meta.core;

import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.annotation.Meta;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Parameter;

@Getter
@Setter
public abstract class MetaParameter{
    protected BaseClass baseClass;
    protected Parameter parameter;
    protected String name;
    public MetaParameter(Parameter parameter) throws LoadClassException {
        this.parameter = parameter;
        Meta meta = parameter.getAnnotation(Meta.class);
        name = meta == null || "".equals(meta.name()) ? parameter.getName() : meta.name();
        MetaClassLoader classLoader = (MetaClassLoader) Thread.currentThread().getContextClassLoader();
        this.baseClass = classLoader.loadClass(parameter.getType());
    }
}
