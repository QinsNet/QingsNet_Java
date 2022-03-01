package com.qins.net.meta.cglib;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.qins.net.core.entity.NetMeta;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.meta.annotation.Meta;
import com.qins.net.meta.core.MetaReferences;
import com.qins.net.meta.standard.ReferenceMetaClass;
import com.qins.net.request.cglib.RequestInterceptor;
import com.qins.net.util.SerializeUtil;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CGLibClass extends ReferenceMetaClass {
    static {
        SerializeUtil.gson = SerializeUtil.gson.newBuilder().registerTypeAdapter(NetMeta.class, new JsonDeserializer<NetMeta>() {
            final Type mapStringType = new TypeToken<HashMap<String,String>>(){}.getType();
            @Override
            public NetMeta deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                NetMeta netMeta = new NetMeta();
                JsonElement instance = jsonObject.get("instance");
                JsonElement nodes = jsonObject.get("nodes");
                if(instance != null){
                    netMeta.setInstance(instance);
                }
                if(nodes != null){
                    netMeta.setNodes(SerializeUtil.gson.fromJson(nodes, mapStringType));
                }
                return netMeta;
            }
        }).create();
    }
    public CGLibClass(Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(instanceClass);
        //Proxy Instance
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(instanceClass);
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
    public <T> T newInstance(Map<String, String> nodes) throws NewInstanceException {
        try {
            for (Map.Entry<String,String> node : this.nodes.entrySet()){
                if(!nodes.containsKey(node.getKey())){
                    nodes.put(node.getKey(),node.getValue());
                }
            }
            Object instance = proxyClass.newInstance();
            ((Factory)instance).setCallbacks(new Callback[]{NoOp.INSTANCE,new RequestInterceptor(request,nodes)});
            return (T) instance;
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new NewInstanceException(e.getCause());
        }
    }
    @Override
    public Object serialize(Object instance, MetaReferences references, Map<String, Object> pools) throws IllegalAccessException {
        if(instance == null)return null;
        Object rawInstance = super.serialize(instance,references,pools);
        RequestInterceptor interceptor = (RequestInterceptor) ((Factory)instance).getCallback(1);
        NetMeta netMeta = new NetMeta(rawInstance,interceptor.getNodes());
        return SerializeUtil.gson.toJsonTree(netMeta,NetMeta.class);
    }
    @Override
    public Object deserialize(Object rawJsonElement, MetaReferences references, Map<String, Object> pools) throws InstantiationException, IllegalAccessException {
        if(rawJsonElement == null)return null;
        JsonElement jsonElement = (JsonElement) rawJsonElement;
        NetMeta netMeta = SerializeUtil.gson.fromJson(jsonElement,NetMeta.class);
        Factory factory;
        if(netMeta.getInstance() != null){
            factory = (Factory) super.deserialize(netMeta.getInstance(), references,pools);
        }
        else factory = (Factory) proxyClass.newInstance();
        factory.setCallbacks(new Callback[]{NoOp.INSTANCE,new RequestInterceptor(request, netMeta.getNodes())});
        return factory;
    }

}
