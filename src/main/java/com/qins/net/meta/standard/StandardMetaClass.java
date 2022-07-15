package com.qins.net.meta.standard;

import com.google.gson.*;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.exception.ObjectLangException;
import com.qins.net.core.exception.SerializeException;
import com.qins.net.core.lang.serialize.FieldLang;
import com.qins.net.core.lang.serialize.ObjectLang;
import com.qins.net.core.lang.serialize.PrimitiveLang;
import com.qins.net.core.lang.serialize.SerializeLang;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.meta.core.MetaField;
import com.qins.net.request.cglib.RequestInterceptor;
import com.qins.net.request.core.RequestReferences;
import com.qins.net.service.core.ServiceReferences;
import com.qins.net.util.AnnotationUtil;
import com.qins.net.util.SerializeUtil;
import lombok.extern.log4j.Log4j2;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

@Log4j2
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
            try {
                if(AnnotationUtil.getMethodPact(method) == null)return 0;
            } catch (ObjectLangException e) {
                onException(e);
                return 0;
            }
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
        log.error(exception);
    }

    @Override
    public void onLog(TrackLog trackLog) {
        log.info(trackLog.toString());
    }

    @Override
    public String serialize(Object instance, SerializeLang serializeLang, RequestReferences references) throws SerializeException {
        //获取引用地址
        int address = System.identityHashCode(instance);
        String value = this.getName()  + "@" +  Integer.toHexString(address);
        //检查是否已经序列化
        if(references.getSerializeObjectsPool().containsKey(value)){
            //Hash相同，对象不一定相同.
            if(references.getSerializeObjectsPool().get(value).equals(instance)){
                return value;
            }
            else {
                //说明是新对象,创建时要注意避免引用池冲突
                do {
                    value = this.getName()  + "@" +  Integer.toHexString(random.nextInt());
                }
                while (references.getSerializeObjectsPool().containsKey(value));
            }
        }
        references.getSerializeObjectsPool().put(value,instance);
        //序列化
        try {
            JsonObject rawInstance = new JsonObject();
            for (Map.Entry<MetaField,SerializeLang> item: serializeFilter(serializeLang,fields).entrySet()){
                MetaField metaField = item.getKey();
                SerializeLang childLang = item.getValue();
                Object object = metaField.getField().get(instance);
                String rawField = StandardMetaSerialize.serialize(object, childLang, references);
                if(rawField == null)rawInstance.add(metaField.getName(),null);
                else rawInstance.add(metaField.getName(),new JsonPrimitive(rawField));
            }
            RequestInterceptor interceptor = (RequestInterceptor) ((Factory)instance).getCallback(1);
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("instance",rawInstance);
            jsonObject.add("nodes", SerializeUtil.gson.toJsonTree(interceptor.getNodes()));
            references.getSerializeDataPool().put(value,jsonObject);
            return value;
        }
        catch (IllegalAccessException e){
            throw new SerializeException(e);
        }
    }
    @Override
    public Object deserialize(String rawInstance, SerializeLang serializeLang, RequestReferences references) throws DeserializeException {
        try {
            //检查是否已经逆序列化
            if(references.getDeserializeObjectsPool().containsKey(rawInstance)){
                return references.getDeserializeObjectsPool().get(rawInstance);
            }
            Object instance = references.getSerializeObjectsPool().get(rawInstance);
            if(instance == null)instance = newInstance();
            references.getDeserializeObjectsPool().put(rawInstance,instance);
            //逆序列化
            JsonObject jsonObject = (JsonObject) references.getDeserializeDataPool().get(rawInstance);
            if(jsonObject == null)return instance;
            JsonElement jsonElement = jsonObject.get("instance");
            if(jsonElement != null){
                JsonObject rawFieldsID = jsonElement.getAsJsonObject();
                for (Map.Entry<MetaField,SerializeLang> item: deserializeFilter(serializeLang,fields).entrySet()){
                    MetaField metaField = item.getKey();
                    SerializeLang childLang = item.getValue();
                    JsonElement rawFieldID = rawFieldsID.get(metaField.getName());
                    if(rawFieldID != null){
                        if(rawFieldID.isJsonNull()){
                            metaField.getField().set(instance,null);
                        }
                        else {
                            Object value = StandardMetaSerialize.deserialize(rawFieldID.getAsString(), childLang, references);
                            metaField.getField().set(instance,value);
                        }
                    }
                }
            }
            JsonElement rawNodes = jsonObject.get("nodes");
            if(rawNodes != null){
                HashMap<String,String> nodes = SerializeUtil.gson.fromJson(rawNodes,HashMap.class);
                ((Factory)instance).setCallbacks(new Callback[]{NoOp.INSTANCE,new RequestInterceptor(request, nodes)});
            }
            return instance;
        }
        catch (NewInstanceException | IllegalAccessException e) {
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
        references.getSerializeLang().put(id,serializeLang);
        //序列化
        try {
            JsonObject rawMeta = new JsonObject();
            JsonObject rawInstance = new JsonObject();
            rawMeta.add("instance",rawInstance);
            RequestInterceptor interceptor = (RequestInterceptor) ((Factory)instance).getCallback(1);
            Map<String,String> newNodes = interceptor.getNodes();
            rawMeta.add("nodes", SerializeUtil.gson.toJsonTree(newNodes));
            for (Map.Entry<MetaField,SerializeLang> item: serializeFilter(serializeLang,fields).entrySet()){
                MetaField metaField = item.getKey();
                SerializeLang childLang = item.getValue();
                Object field = metaField.getField().get(instance);
                String rawField = StandardMetaSerialize.serialize(field, childLang, references);
                if(rawField == null)rawInstance.add(metaField.getName(),null);
                else rawInstance.add(metaField.getName(),new JsonPrimitive(rawField));
            }
            references.getSerializeDataPool().put(id,rawMeta);
            return id;
        }
        catch (IllegalAccessException e){
            throw new SerializeException(e);
        }
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
            JsonObject jsonObject = (JsonObject) references.getDeserializeDataPool().get(rawInstance);
            if(jsonObject == null)return instance;
            JsonElement jsonElement = jsonObject.get("instance");
            if(jsonElement != null){
                JsonObject rawFields = jsonElement.getAsJsonObject();
                for (Map.Entry<MetaField,SerializeLang> item: deserializeFilter(serializeLang,fields).entrySet()){
                    MetaField metaField = item.getKey();
                    SerializeLang childLang = item.getValue();
                    JsonElement rawField = rawFields.get(metaField.getName());
                    if(rawField != null && !rawField.isJsonNull()){
                        Object value = StandardMetaSerialize.deserialize(rawField.getAsString(), childLang, references);
                        metaField.getField().set(instance,value);
                    }
                    else metaField.getField().set(instance,null);
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


    public Map<MetaField,SerializeLang> serializeFilter(SerializeLang serializeLang,Map<String,MetaField> fields){
        Map<MetaField,SerializeLang> fieldsFilter = new LinkedHashMap<>();
        ObjectLang serializeSync = Optional.ofNullable(serializeLang).map(SerializeLang::getSerializeSync).orElse(null);
        ObjectLang serializeAsync = Optional.ofNullable(serializeLang).map(SerializeLang::getSerializeAsync).orElse(null);
        ObjectLang deserializeSync = Optional.ofNullable(serializeLang).map(SerializeLang::getDeserializeSync).orElse(null);
        ObjectLang deserializeAsync = Optional.ofNullable(serializeLang).map(SerializeLang::getDeserializeAsync).orElse(null);
        for (Map.Entry<String,MetaField> item: fields.entrySet()){
            ObjectLang serializeChildSync = null;
            ObjectLang serializeChildAsync = null;
            ObjectLang deserializeChildSync = null;
            ObjectLang deserializeChildAsync = null;
            boolean isSync = false;
            String name = item.getKey();
            if(serializeSync == null)isSync = true;
            else if(serializeSync instanceof PrimitiveLang){
                isSync = true;
            }
            else if(serializeSync instanceof FieldLang && ((FieldLang) serializeSync).getChildren().containsKey(name)){
                isSync = true;
                serializeChildSync = ((FieldLang) serializeSync).getChildren().get(name);
            }
            if(serializeAsync instanceof PrimitiveLang){
                isSync = false;
            }
            else if(serializeAsync instanceof FieldLang && ((FieldLang) serializeAsync).getChildren().containsKey(name)){
                isSync = false;
                serializeChildAsync = ((FieldLang) serializeAsync).getChildren().get(name);
            }
            if(deserializeSync instanceof FieldLang && ((FieldLang) deserializeSync).getChildren().containsKey(name)){
                deserializeChildSync = ((FieldLang) deserializeSync).getChildren().get(name);
            }
            if(deserializeAsync instanceof FieldLang && ((FieldLang) deserializeAsync).getChildren().containsKey(name)){
                deserializeChildAsync = ((FieldLang) deserializeAsync).getChildren().get(name);
            }
            if(isSync)fieldsFilter.put(item.getValue(),
                    new SerializeLang(serializeChildSync,serializeChildAsync,deserializeChildSync,deserializeChildAsync));
        }
        return fieldsFilter;
    }

    public Map<MetaField,SerializeLang> deserializeFilter(SerializeLang serializeLang,Map<String,MetaField> fields){
        Map<MetaField,SerializeLang> fieldsFilter = new LinkedHashMap<>();
        ObjectLang serializeSync = Optional.ofNullable(serializeLang).map(SerializeLang::getSerializeSync).orElse(null);
        ObjectLang serializeAsync = Optional.ofNullable(serializeLang).map(SerializeLang::getSerializeAsync).orElse(null);
        ObjectLang deserializeSync = Optional.ofNullable(serializeLang).map(SerializeLang::getDeserializeSync).orElse(null);
        ObjectLang deserializeAsync = Optional.ofNullable(serializeLang).map(SerializeLang::getDeserializeAsync).orElse(null);
        for (Map.Entry<String,MetaField> item: fields.entrySet()){
            ObjectLang serializeChildSync = null;
            ObjectLang serializeChildAsync = null;
            ObjectLang deserializeChildSync = null;
            ObjectLang deserializeChildAsync = null;
            boolean isSync = false;
            String name = item.getKey();
            if(serializeSync instanceof FieldLang && ((FieldLang) serializeSync).getChildren().containsKey(name)){
                serializeChildSync = ((FieldLang) serializeSync).getChildren().get(name);
            }
            if(serializeAsync instanceof FieldLang && ((FieldLang) serializeAsync).getChildren().containsKey(name)){
                serializeChildAsync = ((FieldLang) serializeAsync).getChildren().get(name);
            }

            if(deserializeSync == null)isSync = true;
            else if(deserializeSync instanceof PrimitiveLang){
                isSync = true;
            }
            else if(deserializeSync instanceof FieldLang && ((FieldLang) deserializeSync).getChildren().containsKey(name)){
                isSync = true;
                deserializeChildSync = ((FieldLang) deserializeSync).getChildren().get(name);
            }
            if(deserializeAsync instanceof PrimitiveLang){
                isSync = false;
            }
            else if(deserializeAsync instanceof FieldLang && ((FieldLang) deserializeAsync).getChildren().containsKey(name)){
                isSync = false;
                deserializeChildAsync = ((FieldLang) deserializeAsync).getChildren().get(name);
            }
            if(isSync)fieldsFilter.put(item.getValue(),
                    new SerializeLang(serializeChildSync,serializeChildAsync,deserializeChildSync,deserializeChildAsync));
        }
        return fieldsFilter;
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
