package com.qins.net.meta.core;

import com.qins.net.meta.annotation.Meta;
import com.qins.net.meta.annotation.Sync;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Parameter;

@Getter
@Setter
public abstract class MetaParameter extends MetaClass{
    protected Parameter parameter;
    protected String name;
    public MetaParameter(Parameter parameter) {
        super(parameter.getType());
        this.parameter = parameter;
        Sync sync = parameter.getAnnotation(Sync.class);
        name = sync.value() != null? sync.value() : parameter.getName();
    }
}
