package com.qins.net.meta.core;


import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.annotation.field.FieldPact;
import com.qins.net.meta.annotation.instance.Meta;
import com.qins.net.util.AnnotationUtil;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

@Getter
@Setter
public abstract class MetaField {
    protected BaseClass baseClass;
    protected Field field;
    protected String name;

    public MetaField(Field field,Components components) throws LoadClassException {
        FieldPact pact = AnnotationUtil.getFieldPact(field);
        assert pact != null;
        this.name = pact.getName();
        this.field = field;
        Class<?> instanceClass = field.getType();
        baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(instanceClass);
    }
}
