package com.qins.net.meta.core;


import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.annotation.Meta;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;

@Getter
@Setter
public abstract class MetaField {
    protected BaseClass baseClass;
    protected Field field;
    protected String name;
    public MetaField(Field field) throws LoadClassException {
        MetaClassLoader metaClassLoader = (MetaClassLoader) Thread.currentThread().getContextClassLoader();
        this.field = field;
        this.baseClass = metaClassLoader.loadClass(field.getType());
        Meta meta = field.getAnnotation(Meta.class);
        name = "".equals(meta.value()) ? field.getName() : meta.value();
    }
}
