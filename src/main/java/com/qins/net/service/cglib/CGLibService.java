package com.qins.net.service.cglib;

import com.qins.net.core.entity.RequestMeta;
import com.qins.net.core.entity.ResponseException;
import com.qins.net.core.entity.ResponseMeta;
import com.qins.net.core.entity.TrackException;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.core.BaseClass;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.meta.core.MetaMethod;
import com.qins.net.meta.core.MetaParameter;
import com.qins.net.service.core.Service;
import com.qins.net.service.core.ServiceContext;
import net.sf.cglib.proxy.Factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;

public class CGLibService extends Service {
    public CGLibService(MetaClass metaClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, LoadClassException, TrackException {
        super(metaClass);
    }
}
