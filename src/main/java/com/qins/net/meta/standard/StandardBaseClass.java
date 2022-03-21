package com.qins.net.meta.standard;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.qins.net.core.console.Console;
import com.qins.net.core.entity.RequestMeta;
import com.qins.net.core.entity.ResponseMeta;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.exception.SerializeException;
import com.qins.net.meta.core.MetaField;
import com.qins.net.meta.core.BaseClass;
import com.qins.net.meta.core.SerializeContext;
import com.qins.net.util.SerializeUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StandardBaseClass extends BaseClass {
    static {
        final Type mapType = new TypeToken<HashMap<String,JsonElement>>(){}.getType();
        SerializeUtil.gson = SerializeUtil.gson.newBuilder().registerTypeAdapter(RequestMeta.class, new JsonDeserializer<RequestMeta>() {
            @Override
            public RequestMeta deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                RequestMeta requestMeta = new RequestMeta();
                requestMeta
                        .setProtocol(jsonObject.get("protocol").getAsString())
                        .setMapping(jsonObject.get("mapping").getAsString())
                        .setInstance(jsonObject.get("instance"))
                        .setParams(SerializeUtil.gson.fromJson(jsonObject.get("params"), mapType))
                        .setReferences(SerializeUtil.gson.fromJson(jsonObject.get("references"), mapType));
                return requestMeta;
            }
        }).registerTypeAdapter(ResponseMeta.class, new JsonDeserializer<ResponseMeta>() {
            @Override
            public ResponseMeta deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                ResponseMeta responseMeta = new ResponseMeta();
                responseMeta
                        .setProtocol(jsonObject.get("protocol").getAsString())
                        .setInstance(jsonObject.get("instance"))
                        .setResult(jsonObject.get("result"))
                        .setParams(SerializeUtil.gson.fromJson(jsonObject.get("params"), mapType))
                        .setReferences(SerializeUtil.gson.fromJson(jsonObject.get("references"), mapType));
                if(jsonObject.get("exception") != null){
                        responseMeta.setException(jsonObject.get("exception").getAsString());
                }
                return responseMeta;
            }
        }).create();
    }
    public StandardBaseClass(String name,Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(name, instanceClass);
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
    public <T> T newInstance() throws NewInstanceException {
        try {
            return (T) instanceClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new NewInstanceException(e);
        }
    }
    @Override
    public Object serialize(Object instance, SerializeContext context) throws SerializeException {
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
            if(instance == null)return null;
            JsonElement rawInstance = export(instance,context);
            context.getSerializePools().put(key.getAsString(),rawInstance);
            return key;
        }
        catch (IllegalAccessException e){
            throw new SerializeException(e);
        }
    }
    @Override
    public Object deserialize(Object rawInstance, SerializeContext context) throws DeserializeException {
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
            context.getDeserializeNames().put(address, key);
            context.getDeserializeObjects().put(key,instance);
            //逆序列化
            JsonElement jsonElement = (JsonElement) context.getDeserializePools().get(key.getAsString());
            update(instance,jsonElement,context);
            return instance;
        }
        catch (NewInstanceException | IllegalAccessException e) {
            throw new DeserializeException(e);
        }
    }
    public void update(Object instance,JsonElement jsonElement,SerializeContext context) throws DeserializeException, IllegalAccessException {
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
    public JsonElement export(Object instance,SerializeContext context) throws SerializeException, IllegalAccessException {
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
}
