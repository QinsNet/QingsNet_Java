package com.qins.net.meta.standard;

import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.core.MetaMethod;
import com.qins.net.meta.core.MetaReturn;

import java.lang.reflect.Method;

public class StandardMetaReturn extends MetaReturn {
    public StandardMetaReturn(Method method, Components components) throws NewInstanceException {
        super(method, components);
    }
}
