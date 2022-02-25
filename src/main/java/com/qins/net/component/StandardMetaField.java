package com.qins.net.component;

import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.core.MetaField;

import java.lang.reflect.Field;

public class StandardMetaField extends MetaField {

    public StandardMetaField(Field field) throws LoadClassException {
        super(field);
    }
}
