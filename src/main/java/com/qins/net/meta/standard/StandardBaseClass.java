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
import com.qins.net.meta.core.ReferencesContext;
import com.qins.net.util.SerializeUtil;
import javafx.util.Pair;

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

    public Object serialize(Object instance, ReferencesContext context) throws SerializeException {
        try {
            if(instance == null)return null;
            else if(instance instanceof Iterable){
                JsonArray jsonArray = new JsonArray();
                for (Object item : ((Iterable)instance)){
                    JsonElement msg = (JsonElement) StandardMetaSerialize.serialize(item, context);
                    jsonArray.add(msg);
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
                    Object a = StandardMetaSerialize.serialize(object, context);
                    jsonObject.add(metaField.getName(), (JsonElement) a);
                }
                return jsonObject;
            }
            else return SerializeUtil.gson.toJsonTree(instance);
        }
        catch (IllegalAccessException e){
            throw new SerializeException(e);
        }
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
    public Object deserialize(Object rawJsonElement, ReferencesContext context) throws DeserializeException {
        try {
            if(rawJsonElement == null) return null;
            Pair<Object,JsonElement> pair = (Pair<Object, JsonElement>) rawJsonElement;
            Object instance = pair.getKey();
            JsonElement jsonElement = pair.getValue();
            if(jsonElement == null || jsonElement.isJsonNull()) return null;
            else if(jsonElement.isJsonPrimitive()){
                instance = SerializeUtil.gson.fromJson(jsonElement, instanceClass);
            }
            else if(jsonElement.isJsonArray() && Collection.class.isAssignableFrom(instanceClass)){
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
            return instance;
        } catch (IllegalAccessException e) {
            throw new DeserializeException(e);
        }
    }
}
