package com.qins.net.meta.core;


import com.qins.net.core.boot.MetaApplication;
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
    protected BaseClass elementClass;
    public MetaField(Field field) throws LoadClassException {
        this.field = field;
        this.baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(field.getType());
        Meta meta = field.getAnnotation(Meta.class);
        name = "".equals(meta.name()) ? field.getName() : meta.name();
        if(meta.element() != Meta.class){
            elementClass = MetaApplication.getContext().getMetaClassLoader().loadClass(meta.element());
        }
    }
}
