package com.qins.net.meta.cglib;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.qins.net.core.entity.QinsMeta;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.exception.NotMetaClassException;
import com.qins.net.meta.annotation.Meta;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.meta.standard.StandardMetaClass;
import com.qins.net.request.cglib.RequestInterceptor;
import com.qins.net.util.SerializeUtil;
import net.sf.cglib.core.ClassGenerator;
import net.sf.cglib.core.GeneratorStrategy;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CGLibClass extends StandardMetaClass {
    static {
        SerializeUtil.gson = SerializeUtil.gson.newBuilder().registerTypeAdapter(QinsMeta.class, new JsonDeserializer<QinsMeta>() {
            final Type mapStringType = new TypeToken<HashMap<String,String>>(){}.getType();
            @Override
            public QinsMeta deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                QinsMeta qinsMeta = new QinsMeta();
                JsonElement instance = jsonObject.get("instance");
                JsonElement nodes = jsonObject.get("nodes");
                if(instance != null){
                    qinsMeta.setInstance(instance.toString());
                }
                if(nodes != null){
                    qinsMeta.setNodes(SerializeUtil.gson.fromJson(nodes, mapStringType));
                }
                return qinsMeta;
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
    public <T> T newInstance(HashMap<String, String> nodes) throws NewInstanceException {
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
    public Object serializeAsObject(Object instance) throws IllegalAccessException {
        if(instance == null)return null;
        Object rawInstance = super.serializeAsObject(instance);
        RequestInterceptor interceptor = (RequestInterceptor) ((Factory)instance).getCallback(1);
        QinsMeta qinsMeta = new QinsMeta(rawInstance,interceptor.getNodes());
        return SerializeUtil.gson.toJsonTree(qinsMeta,QinsMeta.class);
    }

    @Override
    public Object deserializeAsObject(Object rawJsonElement) throws InstantiationException, IllegalAccessException {
        if(rawJsonElement == null)return null;
        JsonElement jsonElement = (JsonElement) rawJsonElement;
        QinsMeta qinsMeta = SerializeUtil.gson.fromJson(jsonElement,QinsMeta.class);
        Factory factory;
        if(qinsMeta.getInstance() != null){
            factory = (Factory) super.deserializeAsObject(SerializeUtil.gson.fromJson((String) qinsMeta.getInstance(),JsonElement.class));
        }
        else factory = (Factory) proxyClass.newInstance();
        factory.setCallbacks(new Callback[]{NoOp.INSTANCE,new RequestInterceptor(request,qinsMeta.getNodes())});
        return factory;
    }

}
