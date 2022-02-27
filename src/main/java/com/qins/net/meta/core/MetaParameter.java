package com.qins.net.meta.core;

import com.qins.net.core.boot.MetaApplication;
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
    protected BaseClass elementClass;
    public MetaParameter(Parameter parameter) throws LoadClassException {
        this.parameter = parameter;
        Meta meta = parameter.getAnnotation(Meta.class);
        name = meta == null || "".equals(meta.name()) ? parameter.getName() : meta.name();
        this.baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(parameter.getType());
        if(meta != null && meta.element() != Meta.class){
            elementClass = MetaApplication.getContext().getMetaClassLoader().loadClass(meta.element());
        }
    }
}
