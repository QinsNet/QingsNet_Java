package com.qins.net.component;

import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.service.core.Service;

import java.lang.reflect.InvocationTargetException;

public class StandardService extends Service {
    public StandardService(MetaClass metaClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, LoadClassException {
        super(metaClass);
    }
}
