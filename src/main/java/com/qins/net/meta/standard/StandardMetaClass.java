package com.qins.net.meta.standard;

import com.google.gson.*;
import com.qins.net.core.console.Console;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.exception.SerializeException;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.meta.core.MetaField;
import com.qins.net.meta.core.ReferencesContext;
import com.qins.net.util.SerializeUtil;
import javafx.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

public abstract class StandardMetaClass extends MetaClass {

    public StandardMetaClass(String name,Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(name,instanceClass);
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
    public Object deserialize(Object rawJsonElement, ReferencesContext context) throws DeserializeException {
        try {
            if(rawJsonElement == null) return null;
            Pair<Object,JsonElement> pair = (Pair<Object, JsonElement>) rawJsonElement;
            Object instance = pair.getKey();
            JsonElement jsonElement = pair.getValue();
            if(jsonElement == null || jsonElement.isJsonNull()) return null;
            else if(jsonElement.isJsonArray() && Collection.class.isAssignableFrom(instanceClass)){
                Collection collection = (Collection) instance;
                for (JsonElement element : jsonElement.getAsJsonArray()){
                    collection.add(StandardMetaSerialize.deserialize(element, context));
                }
            }
            else if(jsonElement.isJsonObject() && Map.class.isAssignableFrom(instanceClass)){
                Map map = (Map) instance;
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
        }
        catch (IllegalAccessException e) {
            throw new DeserializeException(e);
        }
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
