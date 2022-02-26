package com.qins.net.meta.cglib;

import com.google.gson.JsonObject;
import com.qins.net.core.entity.NodeAddress;
import com.qins.net.core.entity.QinsMeta;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.meta.annotation.Meta;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.meta.core.MetaField;
import com.qins.net.meta.standard.StandardMetaClass;
import com.qins.net.request.cglib.CGLibRequest;
import com.qins.net.request.cglib.RequestInterceptor;
import com.qins.net.request.core.Request;
import com.qins.net.util.SerializeUtil;
import com.sun.org.apache.bcel.internal.generic.FADD;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

public class CGLibClass extends StandardMetaClass {
    public CGLibClass(Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(instanceClass);
        //Proxy Instance
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(getInstanceClass());
        enhancer.setCallbackTypes(new Class[]{NoOp.class, RequestInterceptor.class});
        enhancer.setCallbackFilter(method ->
        {
            if(method.getAnnotation(Meta.class) == null)return 0;
            if((method.getModifiers() & Modifier.ABSTRACT) == 0)return 0;
            return 1;
        });
        setProxyClass(enhancer.createClass());
    }

    @Override
    public <T> T newInstance(HashMap<String, String> nodes) throws NewInstanceException {
        try {
            for (Map.Entry<String,String> node : this.nodes.entrySet()){
                if(!nodes.containsKey(node.getKey())){
                    nodes.put(node.getKey(),node.getValue());
                }
            }
            Object instance = proxyClass.newInstance();
            ((Factory)instance).setCallbacks(new Callback[]{NoOp.INSTANCE,new RequestInterceptor((CGLibRequest) request,nodes)});
            return (T) instance;
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new NewInstanceException(e.getCause());
        }
    }

    @Override
    public <T> T newInstance(String rawInstance) throws NewInstanceException {
        try {
            return (T) deserialize(rawInstance);
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new NewInstanceException(e.getCause());
        }
    }

    @Override
    public String serialize(Object instance) throws IllegalAccessException {
        if(instance == null)return null;
        String rawInstance = super.serialize(instance);
        RequestInterceptor interceptor = (RequestInterceptor) ((Factory)instance).getCallback(1);
        QinsMeta qinsMeta = new QinsMeta(rawInstance,interceptor.getNodes());
        JsonObject jsonObject = new JsonObject();
        JsonObject instanceObject = SerializeUtil.gson.fromJson(rawInstance,JsonObject.class);
        jsonObject.add("instance",instanceObject);
        JsonObject nodesObject = SerializeUtil.gson.fromJson(nodes,JsonObject.class);
        jsonObject.add("nodes",instance);
        return SerializeUtil.gson.toJson(qinsMeta,QinsMeta.class);
    }

    @Override
    public Object deserialize(String rawInstance) throws InstantiationException, IllegalAccessException {
        if(rawInstance == null)return null;
        QinsMeta qinsMeta = SerializeUtil.gson.fromJson(rawInstance,QinsMeta.class);
        Factory factory = (Factory) super.deserialize(qinsMeta.getInstance());
        factory.setCallbacks(new Callback[]{NoOp.INSTANCE,new RequestInterceptor((CGLibRequest) request,qinsMeta.getNodes())});
        return factory;
    }

    @Override
    public void sync(Object oldInstance, Object newInstance) throws IllegalAccessException {
        if(newInstance == null)return;
        super.sync(oldInstance, newInstance);
        ((Factory)oldInstance).setCallbacks(((Factory)newInstance).getCallbacks());
    }
}
