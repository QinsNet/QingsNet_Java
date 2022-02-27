package com.qins.net.meta.util;

import com.qins.net.core.exception.NotMetaClassException;
import com.qins.net.meta.cglib.CGLibClass;
import com.qins.net.meta.core.MetaClass;
import net.sf.cglib.proxy.Factory;

import java.util.Arrays;
import java.util.LinkedList;

public class MetaUtil {
    public static MetaClass getMetaClass(Object instance) throws NotMetaClassException {
        if(instance instanceof Factory){
            return CGLibClass.getMetaClass(instance);
        }
        return null;
    }
}
