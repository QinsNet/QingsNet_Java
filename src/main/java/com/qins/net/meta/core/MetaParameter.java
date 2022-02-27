package com.qins.net.meta.core;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.annotation.Meta;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Getter
@Setter
public abstract class MetaParameter{
    protected BaseClass baseClass;
    protected Parameter parameter;
    protected String name;
    public MetaParameter(Parameter parameter, Components components) throws LoadClassException {
        Meta meta = parameter.getAnnotation(Meta.class);
        name = meta == null || "".equals(meta.name()) ? parameter.getName() : meta.name();
        this.parameter = parameter;
        this.baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(parameter.getParameterizedType().getTypeName(),parameter.getType());
        if(parameter.getParameterizedType() instanceof ParameterizedType){
            Type[] actualTypeArguments = ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments();
            baseClass.generics = new BaseClass[actualTypeArguments.length];
            int i = 0;
            for (Type argument : actualTypeArguments){
                if(argument instanceof Class){
                    Class<?> instanceClass = (Class<?>) argument;
                    BaseClass value = MetaApplication.getContext().getMetaClassLoader().loadClass(instanceClass.getName(),instanceClass);
                    baseClass.generics[i++] = value;
                }
            }
        }
    }
}
