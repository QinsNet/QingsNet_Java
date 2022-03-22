package com.qins.net.meta.standard;

import com.google.gson.*;
import com.qins.net.core.console.Console;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.exception.SerializeException;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.meta.core.MetaField;
import com.qins.net.request.cglib.RequestInterceptor;
import com.qins.net.request.core.RequestReferences;
import com.qins.net.service.core.ServiceReferences;
import com.qins.net.util.AnnotationUtil;
import com.qins.net.util.SerializeUtil;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

public class StandardMetaClass extends MetaClass {
    static Random random = new Random();
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
    public String serialize(Object instance, RequestReferences references) throws SerializeException {
        if(instance == null)return null;
        Integer address = System.identityHashCode(instance);
        //检查是否已经序列化
        if(references.getSerializeReferences().containsKey(address)){
            return references.getSerializeReferences().get(address);
        }
        //获取引用地址
        String value = this.getName()  + "@" +  Integer.toHexString(address);
        references.getSerializeReferences().put(address,value);
        references.getSerializeObjects().put(value,instance);
        //序列化
        try {
            JsonElement rawInstance;
            if(instance instanceof Collection){
                JsonArray jsonArray = new JsonArray();
                rawInstance = jsonArray;
                for (Object item : ((Iterable)instance)){
                    String rawItem = StandardMetaSerialize.serialize(item, references);
                    if(rawItem == null)jsonArray.add((String) null);
                    else jsonArray.add(new JsonPrimitive(rawItem));
                }
            }
            else if(instance instanceof Map){
                JsonObject jsonObject = new JsonObject();
                rawInstance = jsonObject;
                for (Map.Entry<Object,Object> item : ((Map<Object,Object>) instance).entrySet()){
                    String rawKey = StandardMetaSerialize.serialize(item, references);
                    String rawValue = StandardMetaSerialize.serialize(item, references);
                    assert rawKey != null;
                    if(rawValue == null)jsonObject.add(rawKey, null);
                    else jsonObject.add(rawKey, new JsonPrimitive(rawValue));
                }
            }
            else {
                JsonObject jsonObject = new JsonObject();
                rawInstance = jsonObject;
                for (MetaField metaField : fields.values()){
                    Object object = metaField.getField().get(instance);
                    if(object == null)continue;
                    String rawField = StandardMetaSerialize.serialize(object, references);
                    if(rawField != null) jsonObject.add(metaField.getName(),new JsonPrimitive(rawField));
                }
            }
            RequestInterceptor interceptor = (RequestInterceptor) ((Factory)instance).getCallback(1);
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("instance",rawInstance);
            jsonObject.add("nodes", SerializeUtil.gson.toJsonTree(interceptor.getNodes()));
            references.getSerializePool().put(value,jsonObject);
            return value;
        }
        catch (IllegalAccessException e){
            throw new SerializeException(e);
        }
    }
    @Override
    public Object deserialize(String rawInstance, RequestReferences references) throws DeserializeException {
        if(rawInstance == null) return null;
        try {
            //检查是否已经逆序列化
            if(references.getDeserializeObjects().containsKey(rawInstance)){
                return references.getDeserializeObjects().get(rawInstance);
            }
            Object instance = references.getSerializeObjects().get(rawInstance);
            if(instance == null)instance = newInstance();
            references.getDeserializeObjects().put(rawInstance,instance);

            //逆序列化
            JsonObject jsonObject = (JsonObject) references.getDeserializePool().get(rawInstance);
            if(jsonObject == null)return instance;
            JsonElement jsonElement = jsonObject.get("instance");
            if(jsonElement != null){
                if(jsonElement.isJsonArray() && Collection.class.isAssignableFrom(instanceClass)){
                    Collection collection = (Collection) instance;
                    for (JsonElement rawItem : jsonElement.getAsJsonArray()){
                        Object item = StandardMetaSerialize.deserialize(rawItem.getAsString(),references);
                        collection.add(item);
                    }
                }
                else if(jsonElement.isJsonObject() && Map.class.isAssignableFrom(instanceClass)){
                    Map map = (Map) instance;
                    for (Map.Entry<String,JsonElement> rawItem : jsonElement.getAsJsonObject().entrySet()){
                        Object key = StandardMetaSerialize.deserialize(rawItem.getKey(),references);
                        Object value = StandardMetaSerialize.deserialize(rawItem.getValue().getAsString(),references);
                        map.put(key,value);
                    }
                }
                if(jsonElement.isJsonObject()){
                    JsonObject rawFields = jsonElement.getAsJsonObject();
                    for (MetaField metaField : fields.values()){
                        Object value = StandardMetaSerialize.deserialize(rawFields.get(metaField.getName()).getAsString(), references);
                        metaField.getField().set(instance,value);
                    }
                }
            }
            JsonElement rawNodes = jsonObject.get("nodes");
            if(rawNodes != null){
                HashMap<String,String> nodes = SerializeUtil.gson.fromJson(jsonObject.get("nodes"),HashMap.class);
                ((Factory)instance).setCallbacks(new Callback[]{NoOp.INSTANCE,new RequestInterceptor(request, nodes)});
            }
            return instance;
        }
        catch (NewInstanceException | IllegalAccessException e) {
            throw new DeserializeException(e);
        }
    }

    @Override
    public String serialize(Object instance, ServiceReferences references) throws SerializeException {
        if(instance == null)return null;
        Integer address = System.identityHashCode(instance);
        //检查是否已经序列化
        if(references.getSerializeReferences().containsKey(address)){
            return references.getSerializeReferences().get(address);
        }
        //获取引用地址
        String value = references.getDeserializeReferences().get(address);
        if(value == null){
            //说明是新对象,创建时要注意避免引用池冲突
            do {
                value = this.getName()  + "@" +  Integer.toHexString(random.nextInt());
            }
            while (references.getDeserializePool().containsKey(value));
        }
        references.getSerializeReferences().put(address,value);
        //序列化
        try {
            JsonElement rawInstance = null;
            if(instance instanceof Collection){
                boolean update = false;
                List<String> oldJsonArray = SerializeUtil.gson.fromJson((JsonElement) references.getDeserializePool().get(value),List.class);
                if(oldJsonArray != null){
                    String[] oldArray = oldJsonArray.toArray(new String[0]);
                    String[] newArray = ((Collection<String>) instance).toArray(new String[0]);
                    if(oldArray.length == newArray.length){
                        for(int i = 0; i< oldArray.length; i++){
                            String newRef = serialize(newArray[i],references);
                            if(newRef == null)newRef = references.getDeserializeReferences().get(System.identityHashCode(newArray[i]));
                            if(!Objects.equals(newRef, oldArray[i])){
                                update=true;
                                break;
                            }
                        }
                    }
                    else update = true;
                }
                else update = true;
                if(update){
                    JsonArray jsonArray = new JsonArray();
                    rawInstance = jsonArray;
                    for (Object item : ((Iterable)instance)){
                        String rawItem = StandardMetaSerialize.serialize(item, references);
                        if(rawItem == null)jsonArray.add((String) null);
                        else jsonArray.add(new JsonPrimitive(rawItem));
                    }
                }
            }
            else if(instance instanceof Map){
                boolean update = false;
                Map<String,String> oldJsonObject = SerializeUtil.gson.fromJson((JsonElement) references.getDeserializePool().get(value), LinkedHashMap.class);
                if(oldJsonObject != null){
                    //Key
                    String[] oldKeys = oldJsonObject.keySet().toArray(new String[0]);
                    Object[] newKeys = ((Map<Object, ?>) instance).keySet().toArray();
                    if(oldKeys.length == newKeys.length){
                        for(int i=0;i<oldKeys.length;i++){
                            String newRef = serialize(newKeys[i],references);
                            if(newRef == null)newRef = references.getDeserializeReferences().get(System.identityHashCode(newKeys[i]));
                            if(!Objects.equals(newRef, oldKeys[i])){
                                update=true;
                                break;
                            }
                        }
                    }
                    else update = true;
                    if(!update){
                        //Key
                        String[] oldValues = oldJsonObject.values().toArray(new String[0]);
                        Object[] newValues = ((Map<Object, ?>) instance).keySet().toArray();
                        if(oldValues.length == newValues.length){
                            for(int i=0;i<oldValues.length;i++){
                                String newRef = serialize(newValues[i],references);
                                if(newRef == null)newRef = references.getDeserializeReferences().get(System.identityHashCode(newValues[i]));
                                if(!Objects.equals(newRef, oldValues[i])){
                                    update=true;
                                    break;
                                }
                            }
                        }
                        else update = true;
                    }
                }
                else update = true;
                if(update){
                    JsonObject jsonObject = new JsonObject();
                    rawInstance = jsonObject;
                    for (Map.Entry<Object,Object> item : ((Map<Object,Object>) instance).entrySet()){
                        String rawKey = StandardMetaSerialize.serialize(item, references);
                        String rawValue = StandardMetaSerialize.serialize(item, references);
                        assert rawKey != null;
                        if(rawValue == null)jsonObject.add(rawKey, null);
                        else jsonObject.add(rawKey, new JsonPrimitive(rawValue));
                    }
                }
            }
            else {
                JsonObject jsonObject = new JsonObject();
                rawInstance = jsonObject;
                for (MetaField metaField : fields.values()){
                    Object object = metaField.getField().get(instance);
                    if(object == null)continue;
                    String rawField = StandardMetaSerialize.serialize(object, references);
                    assert rawField != null;
                    if(references.getSerializePool().containsKey(rawField)){
                        jsonObject.add(metaField.getName(),new JsonPrimitive(rawField));
                    }
                }
            }
            RequestInterceptor interceptor = (RequestInterceptor) ((Factory)instance).getCallback(1);
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("instance",rawInstance);
            jsonObject.add("nodes", SerializeUtil.gson.toJsonTree(interceptor.getNodes()));
            references.getSerializePool().put(value,jsonObject);
            return value;
        }
        catch (IllegalAccessException e){
            throw new SerializeException(e);
        }
    }

    @Override
    public Object deserialize(String rawInstance, ServiceReferences references) throws DeserializeException {
        if(rawInstance == null) return null;
        try {
            //检查是否已经逆序列化
            if(references.getDeserializeObjects().containsKey(rawInstance)){
                return references.getDeserializeObjects().get(rawInstance);
            }
            Object instance = newInstance();
            Integer address = System.identityHashCode(instance);
            references.getDeserializeObjects().put(rawInstance,instance);
            references.getDeserializeReferences().put(address,rawInstance);

            //逆序列化
            JsonObject jsonObject = (JsonObject) references.getDeserializePool().get(rawInstance);
            if(jsonObject == null)return instance;
            JsonElement jsonElement = jsonObject.get("instance");
            if(jsonElement != null){
                if(jsonElement.isJsonArray() && Collection.class.isAssignableFrom(instanceClass)){
                    Collection collection = (Collection) instance;
                    collection.clear();
                    for (JsonElement rawItem : jsonElement.getAsJsonArray()){
                        Object item = StandardMetaSerialize.deserialize(rawItem.getAsString(),references);
                        collection.add(item);
                    }
                }
                else if(jsonElement.isJsonObject() && Map.class.isAssignableFrom(instanceClass)){
                    Map map = (Map) instance;
                    map.clear();
                    for (Map.Entry<String,JsonElement> rawItem : jsonElement.getAsJsonObject().entrySet()){
                        Object key = StandardMetaSerialize.deserialize(rawItem.getKey(),references);
                        Object value = StandardMetaSerialize.deserialize(rawItem.getValue().getAsString(),references);
                        map.put(key,value);
                    }
                }
                else if(jsonElement.isJsonObject()){
                    JsonObject rawFields = jsonElement.getAsJsonObject();
                    for (MetaField metaField : fields.values()){
                        Object value = StandardMetaSerialize.deserialize(rawFields.get(metaField.getName()).getAsString(), references);
                        metaField.getField().set(instance,value);
                    }
                }
            }
            JsonElement rawNodes = jsonObject.get("nodes");
            if(rawNodes != null){
                HashMap<String,String> nodes = SerializeUtil.gson.fromJson(jsonObject.get("nodes"),HashMap.class);
                ((Factory)instance).setCallbacks(new Callback[]{NoOp.INSTANCE,new RequestInterceptor(request, nodes)});
            }
            return instance;
        }
        catch (NewInstanceException | IllegalAccessException e) {
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
