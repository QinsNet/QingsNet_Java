package com.qins.net.meta.standard;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.qins.net.core.entity.RequestMeta;
import com.qins.net.core.entity.ResponseMeta;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.exception.SerializeException;
import com.qins.net.core.lang.serialize.SerializeLang;
import com.qins.net.meta.core.BaseClass;
import com.qins.net.request.core.RequestReferences;
import com.qins.net.service.core.ServiceReferences;
import com.qins.net.util.SerializeUtil;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;

@Log4j2
public class StandardBaseClass extends BaseClass {
    static Random random = new Random();
    static {
        final Type referencesType = new TypeToken<HashMap<String,JsonElement>>(){}.getType();
        final Type paramsType = new TypeToken<HashMap<String,String>>(){}.getType();
        SerializeUtil.gson = SerializeUtil.gson.newBuilder().registerTypeAdapter(RequestMeta.class, new JsonDeserializer<RequestMeta>() {
            @Override
            public RequestMeta deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                RequestMeta requestMeta = new RequestMeta();
                requestMeta
                        .setProtocol(SerializeUtil.getStringOrNull(jsonObject.get("protocol")))
                        .setMapping(SerializeUtil.getStringOrNull(jsonObject.get("mapping")))
                        .setInstance(SerializeUtil.getStringOrNull(jsonObject.get("instance")))
                        .setParams(SerializeUtil.gson.fromJson(jsonObject.get("params"), paramsType))
                        .setReferences(SerializeUtil.gson.fromJson(jsonObject.get("references"), referencesType));
                return requestMeta;
            }
        }).registerTypeAdapter(ResponseMeta.class, new JsonDeserializer<ResponseMeta>() {
            @Override
            public ResponseMeta deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

                JsonObject jsonObject = jsonElement.getAsJsonObject();
                ResponseMeta responseMeta = new ResponseMeta();
                if(jsonObject.get("exception") != null && !jsonObject.get("exception").isJsonNull()){
                        responseMeta.setException(jsonObject.get("exception").getAsString());
                }
                else {
                    responseMeta
                            .setProtocol(SerializeUtil.getStringOrNull(jsonObject.get("protocol")))
                            .setResult(SerializeUtil.getStringOrNull(jsonObject.get("result")))
                            .setReferences(SerializeUtil.gson.fromJson(jsonObject.get("references"), referencesType));
                }
                return responseMeta;
            }
        }).create();
    }
    public StandardBaseClass(String name,Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(name, instanceClass);
    }

    @Override
    public String serialize(Object instance, SerializeLang serializeLang, RequestReferences references) throws SerializeException {
        //获取引用地址
        int address = System.identityHashCode(instance);
        String value = this.getName()  + "@" +  Integer.toHexString(address);
        //检查是否已经序列化
        if(references.getSerializeDataPool().containsKey(value)){
            //Hash相同，对象不一定相同.
            if(references.getSerializeDataPool().get(value).equals(instance)){
                return value;
            }
            else {
                //说明是新对象,创建时要注意避免引用池冲突
                do {
                    value = this.getName()  + "@" +  Integer.toHexString(random.nextInt());
                }
                while (references.getDeserializeDataPool().containsKey(value));
            }
        }
        references.getSerializeObjectsPool().put(value,instance);
        //序列化
        JsonElement rawInstance = null;
        if(instance instanceof Collection){
            JsonArray jsonArray = new JsonArray();
            rawInstance = jsonArray;
            for (Object item : ((Iterable)instance)){
                String rawItem = StandardMetaSerialize.serialize(item, null,references);
                if(rawItem == null)jsonArray.add((String) null);
                else jsonArray.add(new JsonPrimitive(rawItem));
            }
        }
        else if(instance instanceof Map){
            JsonObject jsonObject = new JsonObject();
            rawInstance = jsonObject;
            for (Map.Entry<Object,Object> item : ((Map<Object,Object>) instance).entrySet()){
                String rawKey = StandardMetaSerialize.serialize(item, serializeLang, references);
                String rawValue = StandardMetaSerialize.serialize(item, serializeLang, references);
                assert rawKey != null;
                if(rawValue == null)jsonObject.add(rawKey, null);
                else jsonObject.add(rawKey, new JsonPrimitive(rawValue));
            }
        }
        references.getSerializeDataPool().put(value,rawInstance);
        return value;
    }

    @Override
    public Object deserialize(String rawInstance, SerializeLang serializeLang, RequestReferences references) throws DeserializeException {
        try {
            //检查是否已经逆序列化
            if(references.getDeserializeObjectsPool().containsKey(rawInstance)){
                return references.getDeserializeObjectsPool().get(rawInstance);
            }
            Object instance = newInstance();
            references.getDeserializeObjectsPool().put(rawInstance,instance);
            //逆序列化
            JsonElement jsonElement = (JsonElement) references.getDeserializeDataPool().get(rawInstance);
            if(jsonElement == null)return instance;
            if(jsonElement.isJsonArray() && Collection.class.isAssignableFrom(instanceClass)){
                Collection collection = (Collection) instance;
                ((Collection<?>) instance).clear();
                for (JsonElement rawItem : jsonElement.getAsJsonArray()){
                    Object item = StandardMetaSerialize.deserialize(rawItem.getAsString(), serializeLang, references);
                    collection.add(item);
                }
            }
            else if(jsonElement.isJsonObject() && Map.class.isAssignableFrom(instanceClass)){
                Map map = (Map) instance;
                ((Map<?, ?>) instance).clear();
                for (Map.Entry<String,JsonElement> rawItem : jsonElement.getAsJsonObject().entrySet()){
                    Object key = StandardMetaSerialize.deserialize(rawItem.getKey(), serializeLang, references);
                    Object value = StandardMetaSerialize.deserialize(rawItem.getValue().getAsString(), serializeLang, references);
                    map.put(key,value);
                }
            }
            return instance;
        }
        catch (NewInstanceException e) {
            throw new DeserializeException(e);
        }
    }

    @Override
    public String serialize(Object instance, SerializeLang serializeLang, ServiceReferences references) throws SerializeException {
        //先获得ID
        String id = references.getIds().get(instance);
        if(id == null){
            //说明是新对象，需要生成一个新的ID
            do {
                id = this.getName()  + "@" +  Integer.toHexString(random.nextInt());
            }
            while (references.getIds().containsKey(id));
            references.getIds().put(instance,id);
        }
        //检查是否已经序列化
        if(references.getSerializeObjectsPool().containsKey(id)){
            return id;
        }
        references.getSerializeObjectsPool().put(id,instance);
        //序列化
        JsonElement rawInstance = null;
        if(instance instanceof Collection){
            JsonArray jsonArray = new JsonArray();
            rawInstance = jsonArray;
            for (Object item : ((Iterable)instance)){
                String rawItem = StandardMetaSerialize.serialize(item, null,references);
                if(rawItem == null)jsonArray.add((String) null);
                else jsonArray.add(new JsonPrimitive(rawItem));
            }
        }
        else if(instance instanceof Map){
            JsonObject jsonObject = new JsonObject();
            rawInstance = jsonObject;
            for (Map.Entry<Object,Object> item : ((Map<Object,Object>) instance).entrySet()){
                String rawKey = StandardMetaSerialize.serialize(item, serializeLang, references);
                String rawValue = StandardMetaSerialize.serialize(item, serializeLang, references);
                assert rawKey != null;
                if(rawValue == null)jsonObject.add(rawKey, null);
                else jsonObject.add(rawKey, new JsonPrimitive(rawValue));
            }
        }
        references.getSerializeDataPool().put(id,rawInstance);
        return id;
    }

    @Override
    public Object deserialize(String rawInstance, SerializeLang serializeLang, ServiceReferences references) throws DeserializeException {
        try {
            //检查是否已经逆序列化
            if(references.getDeserializeObjectsPool().containsKey(rawInstance)){
                return references.getDeserializeObjectsPool().get(rawInstance);
            }
            Object instance = newInstance();
            references.getDeserializeObjectsPool().put(rawInstance,instance);
            references.getIds().put(instance,rawInstance);
            references.getSerializeLang().put(rawInstance,serializeLang);
            //逆序列化
            JsonElement jsonElement = (JsonElement) references.getDeserializeDataPool().get(rawInstance);
            if(jsonElement == null)return instance;
            if(jsonElement.isJsonArray() && Collection.class.isAssignableFrom(instanceClass)){
                Collection collection = (Collection) instance;
                ((Collection<?>) instance).clear();
                for (JsonElement rawItem : jsonElement.getAsJsonArray()){
                    Object item = StandardMetaSerialize.deserialize(rawItem.getAsString(), serializeLang, references);
                    collection.add(item);
                }
            }
            else if(jsonElement.isJsonObject() && Map.class.isAssignableFrom(instanceClass)){
                Map map = (Map) instance;
                ((Map<?, ?>) instance).clear();
                for (Map.Entry<String,JsonElement> rawItem : jsonElement.getAsJsonObject().entrySet()){
                    Object key = StandardMetaSerialize.deserialize(rawItem.getKey(), serializeLang, references);
                    Object value = StandardMetaSerialize.deserialize(rawItem.getValue().getAsString(), serializeLang, references);
                    map.put(key,value);
                }
            }
            return instance;
        }
        catch (NewInstanceException e) {
            throw new DeserializeException(e);
        }
    }

    @Override
    public void onException(Exception exception) {
        log.error(exception);
    }

    @Override
    public void onLog(TrackLog trackLog) {
        log.info(trackLog.toString());
    }

    @Override
    public <T> T newInstance() throws NewInstanceException {
        try {
            return (T) instanceClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new NewInstanceException(e);
        }
    }


}
