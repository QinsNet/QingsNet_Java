package com.qins.net.meta.standard;

import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.core.MetaParameter;

import java.lang.reflect.Parameter;

public class StandardMetaParameter extends MetaParameter {
    public StandardMetaParameter(Parameter parameter, Components components) throws LoadClassException {
        super(parameter, components);
    }
}
