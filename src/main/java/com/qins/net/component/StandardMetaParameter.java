package com.qins.net.component;

import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.core.MetaParameter;

import java.lang.reflect.Parameter;

public class StandardMetaParameter extends MetaParameter {
    public StandardMetaParameter(Parameter parameter) throws LoadClassException {
        super(parameter);
    }
}
