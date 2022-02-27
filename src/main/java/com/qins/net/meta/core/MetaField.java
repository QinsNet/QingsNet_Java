package com.qins.net.meta.core;


import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.annotation.Meta;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Getter
@Setter
public abstract class MetaField {
    protected BaseClass baseClass;
    protected Field field;
    protected String name;

    public MetaField(Field field,Components components) throws LoadClassException {
        Meta meta = field.getAnnotation(Meta.class);
        name = "".equals(meta.value()) ? field.getName() : meta.value();
        this.field = field;
        this.baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(field.getGenericType().getTypeName(),field.getType());
        if(field.getGenericType() instanceof ParameterizedType){
            Type[] actualTypeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            baseClass.generics = new MetaClass[actualTypeArguments.length];
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
