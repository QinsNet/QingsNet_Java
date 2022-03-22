package com.qins.net.meta.standard;

import com.google.gson.*;
import com.qins.net.core.console.Console;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.exception.SerializeException;
import com.qins.net.meta.annotation.field.Sync;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.meta.core.MetaField;
import com.qins.net.meta.core.ReferencesContext;
import com.qins.net.request.cglib.RequestInterceptor;
import com.qins.net.util.AnnotationUtil;
import com.qins.net.util.SerializeUtil;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StandardMetaClass extends MetaClass {

    public StandardMetaClass(String name,Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(name,instanceClass);
        //Proxy Instance
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(instanceClass);
        enhancer.setCallbackTypes(new Class[]{NoOp.class, RequestInterceptor.class});
        enhancer.setCallbackFilter(method ->
        {
            if(AnnotationUtil.getMethodPact(method) == null)return 0;
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
    public void onException(Exception exception) {
        exception.printStackTrace();
    }

    @Override
    public void onLog(TrackLog log) {
        Console.log(log.getMessage());
    }
    @Override
    public Object serialize(Object instance, ReferencesContext context) throws SerializeException {
        if(instance == null)return null;
        //引用池检查
        JsonPrimitive key = null;
        Integer address = System.identityHashCode(instance);
        if(context.getSerializeNames().containsKey(address)){
            return context.getSerializeNames().get(address);
        }
        if(context.getDeserializeNames().containsKey(address)){
            key = (JsonPrimitive) context.getDeserializeNames().get(address);
        }
        else key = new JsonPrimitive(this.getName()  + "@" +  Integer.toHexString(instance.hashCode()));
        context.getSerializeNames().put(address, key);
        context.getSerializeObjects().put(key,instance);
        //序列化
        try {
            JsonElement rawInstance = export(instance,context);
            RequestInterceptor interceptor = (RequestInterceptor) ((Factory)instance).getCallback(1);
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("instance",rawInstance);
            jsonObject.add("nodes", SerializeUtil.gson.toJsonTree(interceptor.getNodes()));
            context.getSerializePools().put(key.getAsString(),jsonObject);
            return key;
        }
        catch (IllegalAccessException e){
            throw new SerializeException(e);
        }
    }
    @Override
    public Object deserialize(Object rawInstance, ReferencesContext context) throws DeserializeException {
        if(rawInstance == null) return null;
        try {
            //引用池检查
            Object instance = null;
            JsonPrimitive key = (JsonPrimitive) rawInstance;
            if(context.getDeserializeObjects().containsKey(key)){
                return context.getDeserializeObjects().get(key);
            }
            if(context.getSerializeObjects().containsKey(key)){
                instance = context.getSerializeObjects().get(key);
            }
            else {
                instance = newInstance();
            }
            Integer address = System.identityHashCode(instance);
            context.getDeserializeNames().put(address,key);
            context.getDeserializeObjects().put(key,instance);
            //逆序列化
            JsonObject jsonObject = (JsonObject) context.getDeserializePools().get(key.getAsString());
            update(instance,jsonObject.get("instance"),context);
            HashMap<String,String> nodes = SerializeUtil.gson.fromJson(jsonObject.get("nodes"),HashMap.class);
            ((Factory)instance).setCallbacks(new Callback[]{NoOp.INSTANCE,new RequestInterceptor(request, nodes)});
            return instance;
        }
        catch (NewInstanceException | IllegalAccessException e) {
            throw new DeserializeException(e);
        }
    }
    public void update(Object instance, JsonElement jsonElement, ReferencesContext context) throws DeserializeException, IllegalAccessException {
        if(jsonElement == null)return;
        if(jsonElement.isJsonArray() && Collection.class.isAssignableFrom(instanceClass)){
            Collection collection = (Collection) instance;
            collection.clear();
            for (JsonElement element : jsonElement.getAsJsonArray()){
                collection.add(StandardMetaSerialize.deserialize(element, context));
            }
        }
        else if(jsonElement.isJsonObject() && Map.class.isAssignableFrom(instanceClass)){
            Map map = (Map) instance;
            map.clear();
            for (Map.Entry<String,JsonElement> item : jsonElement.getAsJsonObject().entrySet()){
                map.put(StandardMetaSerialize.deserialize(new JsonPrimitive(item.getKey()), context),StandardMetaSerialize.deserialize(item.getValue(), context));
            }
        }
        else if(jsonElement.isJsonObject()){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (MetaField metaField : fields.values()){
                Object value = StandardMetaSerialize.deserialize(jsonObject.get(metaField.getName()), context);
                if(value == null)continue;
                metaField.getField().set(instance,value);
            }
        }
    }
    public JsonElement export(Object instance, ReferencesContext context) throws SerializeException, IllegalAccessException {
        if(instance == null)return null;
        if(instance instanceof Iterable){
            JsonArray jsonArray = new JsonArray();
            for (Object item : ((Iterable)instance)){
                jsonArray.add((JsonPrimitive) StandardMetaSerialize.serialize(item, context));
            }
            return jsonArray;
        }
        else if(instance instanceof Map){
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<Object,Object> item : ((Map<Object,Object>) instance).entrySet()){
                jsonObject.add(((JsonPrimitive)StandardMetaSerialize.serialize(item.getKey(), context)).getAsString(), (JsonElement) StandardMetaSerialize.serialize(instance, context));
            }
            return jsonObject;
        }
        else if(fields.size() != 0){
            JsonObject jsonObject = new JsonObject();
            for (MetaField metaField : fields.values()){
                Object object = metaField.getField().get(instance);
                if(object == null)continue;
                jsonObject.add(metaField.getName(), (JsonPrimitive) StandardMetaSerialize.serialize(object, context));
            }
            return jsonObject;
        }
        else return null;
    }
    @Override
    public <T> T newInstance() throws NewInstanceException {
        try {
            return (T) proxyClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new NewInstanceException(e);
        }
    }
}
