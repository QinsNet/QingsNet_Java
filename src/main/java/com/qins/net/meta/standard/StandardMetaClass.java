package com.qins.net.meta.standard;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.qins.net.core.console.Console;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.meta.core.MetaField;
import com.qins.net.meta.core.MetaReferences;
import com.qins.net.util.SerializeUtil;
import javafx.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

public abstract class StandardMetaClass extends MetaClass {

    public StandardMetaClass(Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
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

    public Object serialize(Object instance, MetaReferences references, Map<String, Object> pools) throws IllegalAccessException {
        if(instance == null)return null;
        else if(generics != null && instance instanceof Iterable){
            JsonArray jsonArray = new JsonArray();
            for (Object item : ((Iterable)instance)){
                JsonElement msg = (JsonElement) generics[0].serialize(item, references,pools);
                jsonArray.add(msg);
            }
            return jsonArray;
        }
        else if(generics != null && instance instanceof Map){
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<Object,Object> item : ((Map<Object,Object>) instance).entrySet()){
                jsonObject.add(((JsonPrimitive)generics[0].serialize(item.getKey(),references,pools)).getAsString(), (JsonElement) generics[1].serialize(instance, references,pools));
            }
            return jsonObject;
        }
        else if(fields.size() != 0){
            JsonObject jsonObject = new JsonObject();
            for (MetaField metaField : fields.values()){
                Object object = metaField.getField().get(instance);
                if(object == null)continue;
                Object a = metaField.getBaseClass().serialize(object,references,pools);
                jsonObject.add(metaField.getName(), (JsonElement) a);
            }
            return jsonObject;
        }
        else return SerializeUtil.gson.toJsonTree(instance);
    }
    @Override
    public Object deserialize(Object rawJsonElement, MetaReferences references, Map<String, Object> pools) throws InstantiationException, IllegalAccessException {
        Pair<Object,JsonElement> pair = (Pair<Object, JsonElement>) rawJsonElement;
        Object instance = pair.getKey();
        JsonElement jsonElement = pair.getValue();
        if(jsonElement.isJsonNull()) return null;
        else if(generics != null && jsonElement.isJsonArray()){
            Collection collection = (Collection) instance;
            for (JsonElement element : jsonElement.getAsJsonArray()){
                collection.add(generics[0].deserialize(element, references, pools));
            }
        }
        else if(generics != null && jsonElement.isJsonObject() && Map.class.isAssignableFrom(instanceClass)){
            Map map = (Map) instance;
            for (Map.Entry<String,JsonElement> item : jsonElement.getAsJsonObject().entrySet()){
                map.put(generics[0].deserialize(new JsonPrimitive(item.getKey()),references,pools),generics[1].deserialize(item.getValue(), references, pools));
            }
        }
        else if(jsonElement.isJsonObject()){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (MetaField metaField : fields.values()){
                Object value = metaField.getBaseClass().deserialize(jsonObject.get(metaField.getName()),references,pools);
                if(value == null)continue;
                metaField.getField().set(instance,value);
            }
        }
        return instance;
    }
}
