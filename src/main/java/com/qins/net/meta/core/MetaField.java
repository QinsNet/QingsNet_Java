package com.qins.net.meta.core;


import com.qins.net.meta.annotation.Meta;
import com.qins.net.meta.annotation.Sync;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;

@Getter
@Setter
public abstract class MetaField extends MetaClass{
    protected Field field;
    public MetaField(Field field)  {
        super(field.getType());
        this.field = field;
        Sync sync = field.getAnnotation(Sync.class);
        name = sync.value() != null? sync.value() : field.getName();
    }
}
