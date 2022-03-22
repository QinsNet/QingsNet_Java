package com.qins.net.meta.core;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.annotation.field.Sync;
import com.qins.net.meta.annotation.parameter.ParameterPact;
import com.qins.net.util.AnnotationUtil;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Parameter;

@Getter
@Setter
public abstract class MetaParameter{
    protected BaseClass baseClass;
    protected Parameter parameter;
    protected String name;
    protected boolean sync;
    public MetaParameter(Parameter parameter, Components components) throws LoadClassException {
        ParameterPact pact = AnnotationUtil.getParameterPact(parameter);
        if(pact == null){
            pact = new ParameterPact().setName(parameter.getName()).setSync(true);
        }
        this.name = pact.getName();
        this.sync = pact.isSync();
        this.parameter = parameter;
        this.baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(parameter.getType());
    }
}
