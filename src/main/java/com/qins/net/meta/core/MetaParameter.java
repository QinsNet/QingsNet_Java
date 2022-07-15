package com.qins.net.meta.core;

import com.qins.net.core.exception.LoadClassException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.lang.serialize.SerializeLang;
import com.qins.net.meta.annotation.Components;
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
    SerializeLang serializeLang;

    public MetaParameter(Parameter parameter, Components components) throws NewInstanceException {
        try {
            this.parameter = parameter;
            ParameterPact pact = AnnotationUtil.getParameterPact(parameter);
            assert pact != null;
            this.name = pact.getName();
            this.serializeLang = pact.getSerializeLang();
        }
        catch (Exception e){
            throw new NewInstanceException(e);
        }
    }
}
