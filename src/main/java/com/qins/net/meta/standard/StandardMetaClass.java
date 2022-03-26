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
        Integer address = System.identityHashCode(instance);
        //检查是否已经序列化
        if(references.getSerializeReferences().containsKey(address)){
            return references.getSerializeReferences().get(address);
        }
        //获取引用地址
        String value = this.getName()  + "@" +  Integer.toHexString(address);
        references.getSerializeReferences().put(address,value);
        references.getSerializeObjects().put(value,instance);
        references.getSerializeLang().put(address,serializeLang);
        //序列化
        try {
            JsonObject rawInstance = new JsonObject();
            for (Map.Entry<MetaField,SerializeLang> item: requestFilter(serializeLang,fields).entrySet()){
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
            references.getSerializePool().put(value,jsonObject);
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
            if(references.getDeserializeObjects().containsKey(rawInstance)){
                return references.getDeserializeObjects().get(rawInstance);
            }
            Object instance = references.getSerializeObjects().get(rawInstance);
            if(instance == null)instance = newInstance();
            else serializeLang = references.getSerializeLang().get(System.identityHashCode(instance));
            references.getDeserializeObjects().put(rawInstance,instance);
            //逆序列化
            JsonObject jsonObject = (JsonObject) references.getDeserializePool().get(rawInstance);
            if(jsonObject == null)return instance;
            JsonElement jsonElement = jsonObject.get("instance");
            if(jsonElement != null){
                JsonObject rawFields = jsonElement.getAsJsonObject();
                for (Map.Entry<MetaField,SerializeLang> item: requestFilter(serializeLang,fields).entrySet()){
                    MetaField metaField = item.getKey();
                    SerializeLang childLang = item.getValue();
                    JsonElement rawField = rawFields.get(metaField.getName());
                    if(rawField != null){
                        if(rawField.isJsonNull()){
                            metaField.getField().set(instance,null);
                        }
                        else {
                            Object value = StandardMetaSerialize.deserialize(rawField.getAsString(), childLang, references);
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
        else serializeLang = references.getSerializeLang().get(value);
        references.getSerializeReferences().put(address,value);

        //序列化
        try {
            JsonObject oldRawMeta = (JsonObject) references.getDeserializePool().get(value);
            JsonObject rawMeta = new JsonObject();
            JsonObject rawInstance = new JsonObject();
            rawMeta.add("instance",rawInstance);
            if(oldRawMeta != null){//是否存在旧Meta
                JsonObject oldRawInstance = (JsonObject) oldRawMeta.get("instance");
                if(oldRawInstance != null){//是否存在旧Instance
                    for (Map.Entry<MetaField,SerializeLang> item: serviceFilter(serializeLang,fields).entrySet()){
                        MetaField metaField = item.getKey();
                        SerializeLang childLang = item.getValue();
                        Object field = metaField.getField().get(instance);
                        String newRawField = references.getDeserializeReferences().get(System.identityHashCode(field));
                        if(newRawField == null) newRawField = StandardMetaSerialize.serialize(field, childLang, references);
                        String oldRawField = SerializeUtil.getStringOrNull(oldRawInstance.get(metaField.getName()));
                        if(!Objects.equals(newRawField, oldRawField)){
                            String rawField = StandardMetaSerialize.serialize(field, childLang, references);
                            if(rawField == null)rawInstance.add(metaField.getName(),null);
                            else rawInstance.add(metaField.getName(),new JsonPrimitive(rawField));
                        }
                    }
                }
                else {//生成新的Instance
                    for (Map.Entry<MetaField,SerializeLang> item: serviceFilter(serializeLang,fields).entrySet()){
                        MetaField metaField = item.getKey();
                        SerializeLang childLang = item.getValue();
                        Object field = metaField.getField().get(instance);
                        String rawField = StandardMetaSerialize.serialize(field, childLang, references);
                        if(rawField == null)rawInstance.add(metaField.getName(),null);
                        else rawInstance.add(metaField.getName(),new JsonPrimitive(rawField));
                    }
                }
                Object oldRawNodes = oldRawMeta.get("nodes");
                if(oldRawNodes != null){//是否存在旧Nodes
                    RequestInterceptor interceptor = (RequestInterceptor) ((Factory)instance).getCallback(1);
                    Map<String,String> oldNodes = SerializeUtil.gson.fromJson(oldRawMeta.get("nodes"),HashMap.class);
                    Map<String,String> newNodes = interceptor.getNodes();
                    if(newNodes != null && oldNodes != null){//是否存在新Nodes
                        for(Map.Entry<String,String> item : oldNodes.entrySet()){
                            if(!newNodes.containsKey(item.getKey()) || !Objects.equals(newNodes.get(item.getKey()),item.getValue())){
                                rawMeta.add("nodes", SerializeUtil.gson.toJsonTree(newNodes));
                                break;
                            }
                        }
                    }
                    else rawMeta.add("nodes",null);
                }
                else {//生成新Nodes
                    RequestInterceptor interceptor = (RequestInterceptor) ((Factory)instance).getCallback(1);
                    Map<String,String> newNodes = interceptor.getNodes();
                    rawMeta.add("nodes", SerializeUtil.gson.toJsonTree(newNodes));
                }
            }
            else {//生成新Meta
                for (Map.Entry<MetaField,SerializeLang> item: serviceFilter(serializeLang,fields).entrySet()){
                    MetaField metaField = item.getKey();
                    SerializeLang childLang = item.getValue();
                    Object field = metaField.getField().get(instance);
                    String rawField = StandardMetaSerialize.serialize(field, childLang, references);
                    if(rawField == null)rawInstance.add(metaField.getName(),null);
                    else rawInstance.add(metaField.getName(),new JsonPrimitive(rawField));
                }
                RequestInterceptor interceptor = (RequestInterceptor) ((Factory)instance).getCallback(1);
                Map<String,String> newNodes = interceptor.getNodes();
                rawMeta.add("nodes", SerializeUtil.gson.toJsonTree(newNodes));
            }
            references.getSerializePool().put(value,rawMeta);
            return value;
        }
        catch (IllegalAccessException e){
            throw new SerializeException(e);
        }
    }

    @Override
    public Object deserialize(String rawInstance, SerializeLang serializeLang, ServiceReferences references) throws DeserializeException {
        try {
            //检查是否已经逆序列化
            if(references.getDeserializeObjects().containsKey(rawInstance)){
                return references.getDeserializeObjects().get(rawInstance);
            }
            Object instance = newInstance();
            Integer address = System.identityHashCode(instance);
            references.getDeserializeObjects().put(rawInstance,instance);
            references.getDeserializeReferences().put(address,rawInstance);
            references.getSerializeLang().put(rawInstance,serializeLang);
            //逆序列化
            JsonObject jsonObject = (JsonObject) references.getDeserializePool().get(rawInstance);
            if(jsonObject == null)return instance;
            JsonElement jsonElement = jsonObject.get("instance");
            if(jsonElement != null){
                JsonObject rawFields = jsonElement.getAsJsonObject();
                for (Map.Entry<MetaField,SerializeLang> item: serviceFilter(serializeLang,fields).entrySet()){
                    MetaField metaField = item.getKey();
                    SerializeLang childLang = item.getValue();
                    JsonElement rawField = rawFields.get(metaField.getName());
                    if(rawField != null && !rawField.isJsonNull()){
                        Object value = StandardMetaSerialize.deserialize(rawField.getAsString(), childLang, references);
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


    public Map<MetaField,SerializeLang> requestFilter(SerializeLang serializeLang,Map<String,MetaField> fields){
        Map<MetaField,SerializeLang> fieldsFilter = new LinkedHashMap<>();
        ObjectLang requestSync = Optional.ofNullable(serializeLang).map(SerializeLang::getRequestSync).orElse(null);
        ObjectLang requestAsync = Optional.ofNullable(serializeLang).map(SerializeLang::getRequestAsync).orElse(null);
        ObjectLang serviceSync = Optional.ofNullable(serializeLang).map(SerializeLang::getServiceSync).orElse(null);
        ObjectLang serviceAsync = Optional.ofNullable(serializeLang).map(SerializeLang::getServiceAsync).orElse(null);
        for (Map.Entry<String,MetaField> item: fields.entrySet()){
            ObjectLang requestChildSync = null;
            ObjectLang requestChildAsync = null;
            ObjectLang serviceChildSync = null;
            ObjectLang serviceChildAsync = null;
            boolean isSync = false;
            String name = item.getKey();
            if(requestSync == null)isSync = true;
            else if(requestSync instanceof PrimitiveLang){
                isSync = true;
            }
            else if(requestSync instanceof FieldLang && ((FieldLang) requestSync).getChildren().containsKey(name)){
                isSync = true;
                requestChildSync = ((FieldLang) requestSync).getChildren().get(name);
            }
            if(requestAsync instanceof PrimitiveLang){
                isSync = false;
            }
            else if(requestAsync instanceof FieldLang && ((FieldLang) requestAsync).getChildren().containsKey(name)){
                requestChildAsync = ((FieldLang) requestAsync).getChildren().get(name);
            }
            if(serviceSync instanceof FieldLang && ((FieldLang) serviceSync).getChildren().containsKey(name)){
                serviceChildSync = ((FieldLang) serviceSync).getChildren().get(name);
            }
            if(serviceAsync instanceof FieldLang && ((FieldLang) serviceAsync).getChildren().containsKey(name)){
                serviceChildAsync = ((FieldLang) serviceAsync).getChildren().get(name);
            }
            if(isSync)fieldsFilter.put(item.getValue(),new SerializeLang(requestChildSync,requestChildAsync,serviceChildSync,serviceChildAsync));
        }
        return fieldsFilter;
    }

    public Map<MetaField,SerializeLang> serviceFilter(SerializeLang serializeLang,Map<String,MetaField> fields){
        Map<MetaField,SerializeLang> fieldsFilter = new LinkedHashMap<>();
        ObjectLang requestSync = Optional.ofNullable(serializeLang).map(SerializeLang::getRequestSync).orElse(null);
        ObjectLang requestAsync = Optional.ofNullable(serializeLang).map(SerializeLang::getRequestAsync).orElse(null);
        ObjectLang serviceSync = Optional.ofNullable(serializeLang).map(SerializeLang::getServiceSync).orElse(null);
        ObjectLang serviceAsync = Optional.ofNullable(serializeLang).map(SerializeLang::getServiceAsync).orElse(null);
        for (Map.Entry<String,MetaField> item: fields.entrySet()){
            ObjectLang requestChildSync = null;
            ObjectLang requestChildAsync = null;
            ObjectLang serviceChildSync = null;
            ObjectLang serviceChildAsync = null;
            boolean isSync = false;
            String name = item.getKey();
            if(requestSync instanceof FieldLang && ((FieldLang) requestSync).getChildren().containsKey(name)){
                requestChildSync = ((FieldLang) requestSync).getChildren().get(name);
            }
            if(requestAsync instanceof FieldLang && ((FieldLang) requestAsync).getChildren().containsKey(name)){
                requestChildAsync = ((FieldLang) requestAsync).getChildren().get(name);
            }

            if(serviceSync == null)isSync = true;
            else if(serviceSync instanceof PrimitiveLang){
                isSync = true;
            }
            else if(serviceSync instanceof FieldLang && ((FieldLang) serviceSync).getChildren().containsKey(name)){
                isSync = true;
                serviceChildSync = ((FieldLang) serviceSync).getChildren().get(name);
            }
            if(serviceAsync instanceof PrimitiveLang){
                isSync = false;
            }
            else if(serviceAsync instanceof FieldLang && ((FieldLang) serviceAsync).getChildren().containsKey(name)){
                serviceChildAsync = ((FieldLang) serviceAsync).getChildren().get(name);
            }
            if(isSync)fieldsFilter.put(item.getValue(),new SerializeLang(requestChildSync,requestChildAsync,serviceChildSync,serviceChildAsync));
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
