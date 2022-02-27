package com.qins.net.service.standard;

import com.qins.net.core.exception.TrackException;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.service.core.Service;

import java.lang.reflect.InvocationTargetException;

public class StandardService extends Service {
    public StandardService(MetaClass metaClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, LoadClassException, TrackException {
        super(metaClass);
    }
}
