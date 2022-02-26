package com.qins.net.request.cglib;

import com.qins.net.core.entity.ResponseMeta;
import com.qins.net.meta.annotation.Meta;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.meta.core.MetaMethod;
import com.qins.net.meta.core.MetaParameter;
import com.qins.net.node.core.Node;
import com.qins.net.request.core.Request;
import com.qins.net.request.core.RequestContext;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CGLibRequest extends Request {
    public CGLibRequest(MetaClass metaClass) {
        super(metaClass);
    }

    public Object intercept(Object instance, Method method, Object[] args, MethodProxy methodProxy,HashMap<String,String> nodes) throws Exception {
        return intercept(instance,method,args,nodes);
    }

}
