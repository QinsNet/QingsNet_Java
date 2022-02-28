package com.qins.net.meta.standard;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.qins.net.core.console.Console;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.meta.core.MetaField;
import com.qins.net.meta.core.BaseClass;
import com.qins.net.meta.core.MetaReferences;
import com.qins.net.util.SerializeUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

public abstract class StandardBaseClass extends BaseClass {

    public StandardBaseClass(Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(instanceClass);
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
    public Object serializeAsObject(Object instance, MetaReferences references, Map<String, String> pools) throws IllegalAccessException {
        if(instance == null)return null;
        else if(generics != null && instance instanceof Iterable){
            JsonArray jsonArray = new JsonArray();
            for (Object item : ((Iterable)instance)){
                JsonElement msg = (JsonElement) generics[0].serializeAsObject(item, references,pools);
                jsonArray.add(msg);
            }
            return jsonArray;
        }
        else if(generics != null && instance instanceof Map){
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<Object,Object> item : ((Map<Object,Object>) instance).entrySet()){
                jsonObject.add(generics[0].serialize(item.getKey(),references,pools), (JsonElement) generics[1].serializeAsObject(instance, references,pools));
            }
            return jsonObject;
        }
        else if(fields.size() != 0){
            JsonObject jsonObject = new JsonObject();
            for (MetaField metaField : fields.values()){
                Object object = metaField.getField().get(instance);
                if(object == null)continue;
                Object a = metaField.getBaseClass().serializeAsObject(object,references,pools);
                jsonObject.add(metaField.getName(), (JsonElement) a);
            }
            return jsonObject;
        }
        else return SerializeUtil.gson.toJsonTree(instance);
    }
    @Override
    public Object deserializeAsObject(Object rawJsonElement, MetaReferences references, Map<String, String> pools) throws InstantiationException, IllegalAccessException {
        JsonElement jsonElement = (JsonElement) rawJsonElement;
        if(rawJsonElement == null)return null;
        else if(jsonElement.isJsonNull()) return null;
        else if(generics != null && jsonElement.isJsonArray()){
            Collection instance = (Collection) instanceClass.newInstance();
            for (JsonElement element : jsonElement.getAsJsonArray()){
                instance.add(generics[0].deserializeAsObject(element, references, pools));
            }
            return instance;
        }
        else if(generics != null && jsonElement.isJsonObject() && Map.class.isAssignableFrom(instanceClass)){
            Map map = (Map) instanceClass.newInstance();
            for (Map.Entry<String,JsonElement> item : jsonElement.getAsJsonObject().entrySet()){
                map.put(generics[0].deserialize(item.getKey(),references,pools),generics[1].deserializeAsObject(item.getValue(), references, pools));
            }
            return map;
        }
        else if(jsonElement.isJsonObject()){
            Object baseInstance = instanceClass.newInstance();
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (MetaField metaField : fields.values()){
                Object value = metaField.getBaseClass().deserializeAsObject(jsonObject.get(metaField.getName()),references,pools);
                if(value == null)continue;
                metaField.getField().set(baseInstance,value);
            }
            return baseInstance;
        }
        else return SerializeUtil.gson.fromJson(jsonElement,instanceClass);
    }

    @Override
    public String serialize(Object instance, MetaReferences references, Map<String, String> pools) throws IllegalAccessException {
        return SerializeUtil.gson.toJson(serializeAsObject(instance,references,pools));
    }

    @Override
    public Object deserialize(String reference, MetaReferences references, Map<String, String> pools) throws InstantiationException, IllegalAccessException {
        return deserializeAsObject(SerializeUtil.gson.fromJson(reference,JsonElement.class), references, pools);
    }
}
