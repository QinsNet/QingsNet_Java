package com.qins.net.meta.standard;

import com.qins.net.core.exception.LoadClassException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.exception.TrackException;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.core.MetaMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StandardMetaMethod extends MetaMethod {
    public StandardMetaMethod(Method method, Components components) throws NewInstanceException {
        super(method, components);
    }
}
