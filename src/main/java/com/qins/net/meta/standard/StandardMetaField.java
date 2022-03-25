package com.qins.net.meta.standard;

import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.core.BaseClass;
import com.qins.net.meta.core.MetaField;

import java.lang.reflect.Field;

public class StandardMetaField extends MetaField {

    public StandardMetaField(Field field, Components components) throws LoadClassException {
        super(field, components);
    }
}
