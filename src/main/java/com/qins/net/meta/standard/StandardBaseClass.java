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
import com.qins.net.meta.core.BaseClass;
import com.qins.net.meta.core.ReferencesContext;
import com.qins.net.request.core.RequestReferences;
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
                        .setInstance(SerializeUtil.gson.fromJson(jsonObject.get("instance"), mapType))
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
                        .setResult(jsonObject.get("result"))
                        .setInstance(SerializeUtil.gson.fromJson(jsonObject.get("instance"), mapType))
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
    public String serialize(Object instance, RequestReferences references) throws SerializeException {
        if(instance == null)return null;
        //引用池检查
        if(references.getSerializeValues().containsKey(instance)){
            return references.getSerializeValues().get(instance);
        }
        String key = this.getName()  + "@" +  Integer.toHexString(instance.hashCode());
        references.getSerializeValues().put(instance, key);
        references.getSerializeObjects().put(key,instance);
        //序列化
        if(instance instanceof Iterable){
            JsonArray jsonArray = new JsonArray();
            for (Object item : ((Iterable)instance)){
                String rawItem = StandardMetaSerialize.serialize(item, references);
                if(rawItem == null)continue;
                jsonArray.add(new JsonPrimitive(rawItem));
            }
            if(jsonArray.size() == 0)return null;
            references.getSerializePool().put(key,jsonArray);
        }
        else if(instance instanceof Map){
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<Object,Object> item : ((Map<Object,Object>) instance).entrySet()){
                String rawItemKey = StandardMetaSerialize.serialize(item.getKey(), references);
                String rawItemValue = StandardMetaSerialize.serialize(item.getValue(), references);
                if(rawItemKey == null || rawItemValue == null)continue;
                jsonObject.add(rawItemKey,new JsonPrimitive(rawItemValue));
            }
            if(jsonObject.size() == 0)return null;
            references.getSerializePool().put(key,jsonObject);
        }
        return key;
    }

    @Override
    public Object deserialize(String rawInstance, RequestReferences references) throws DeserializeException, NewInstanceException {
        if(rawInstance == null)return null;
        if(references.getDeserializeObjects().containsKey(rawInstance)){
            return references.getDeserializeObjects().get(rawInstance);
        }
        Object instance = references.getSerializeObjects().get(rawInstance);
        if(instance == null)instance = newInstance();
        references.getDeserializeObjects().put(rawInstance,instance);
        JsonElement newRawInstance = (JsonElement) references.getDeserializePool().get(rawInstance);
        if(newRawInstance == null)return null;
        if(newRawInstance.isJsonArray() && Collection.class.isAssignableFrom(instanceClass)){
            Collection collection = (Collection) instance;
            collection.clear();
            for (JsonElement element : newRawInstance.getAsJsonArray()){
                collection.add(StandardMetaSerialize.deserialize(element.getAsString(), references));
            }
        }
        else if(newRawInstance.isJsonObject() && Map.class.isAssignableFrom(instanceClass)){
            Map map = (Map) instance;
            map.clear();
            for (Map.Entry<String,JsonElement> item : newRawInstance.getAsJsonObject().entrySet()){
                map.put(StandardMetaSerialize.deserialize(item.getKey(), references),StandardMetaSerialize.deserialize(item.getValue().getAsString(), references));
            }
        }
        return instance;
    }


    @Override
    public Object deserializeService(String rawInstance, ReferencesContext context) throws DeserializeException, NewInstanceException {
        if(rawInstance == null)return null;
        if(context.getDeserializeObjects().containsKey(rawInstance)){
            return context.getDeserializeObjects().get(rawInstance);
        }
        Object instance = newInstance();
        context.getDeserializeObjects().put(rawInstance,instance);
        context.getDeserializeValues().put(instance,rawInstance);
        JsonElement newRawInstance = (JsonElement) context.getDeserializePool().get(rawInstance);
        if(newRawInstance == null)return null;
        if(newRawInstance.isJsonArray() && Collection.class.isAssignableFrom(instanceClass)){
            Collection collection = (Collection) instance;
            collection.clear();
            for (JsonElement element : newRawInstance.getAsJsonArray()){
                collection.add(StandardMetaSerialize.deserialize(element.getAsString(),context));
            }
        }
        else if(newRawInstance.isJsonObject() && Map.class.isAssignableFrom(instanceClass)){
            Map map = (Map) instance;
            map.clear();
            for (Map.Entry<String,JsonElement> item : newRawInstance.getAsJsonObject().entrySet()){
                map.put(StandardMetaSerialize.deserialize(item.getKey(), context),StandardMetaSerialize.deserialize(item.getValue().getAsString(), context));
            }
        }
        return instance;
    }

}
