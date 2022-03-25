package com.qins.net.service.standard;

import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.exception.TrackException;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.service.core.Service;

import java.lang.reflect.InvocationTargetException;

public class StandardService extends Service {
    public StandardService(MetaClass metaClass) throws NewInstanceException {
        super(metaClass);
    }
}
