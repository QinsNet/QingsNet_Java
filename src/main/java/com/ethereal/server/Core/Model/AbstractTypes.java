package com.ethereal.server.Core.Model;
import com.ethereal.server.Utils.Utils;

import java.lang.reflect.Type;
import java.util.HashMap;

public class AbstractTypes {
    public interface IConvert {
        Object convert(Object obj);
    }
    private HashMap<Type, AbstractType> typesByType = new HashMap<>();
    private HashMap<String, AbstractType> typesByName = new HashMap<>();

    public HashMap<Type, AbstractType> getTypesByType() {
        return typesByType;
    }

    public void setTypesByType(HashMap<Type, AbstractType> typesByType) {
        this.typesByType = typesByType;
    }

    public HashMap<String, AbstractType> getTypesByName() {
        return typesByName;
    }

    public void setTypesByName(HashMap<String, AbstractType> typesByName) {
        this.typesByName = typesByName;
    }

    public AbstractTypes(){

    }
    public void add(Type type, String abstractName) throws TrackException {
        if (typesByName.containsKey(abstractName) || typesByType.containsKey(type)) throw new TrackException(TrackException.ErrorCode.Core,String.format("类型:{%s}转{%s}发生异常,存在重复键",type, abstractName));
        else{
            AbstractType rpcType = new AbstractType();
            rpcType.setName(abstractName);
            rpcType.setType(type);
            rpcType.setDeserialize(obj -> Utils.gson.fromJson(obj,type));
            rpcType.setSerialize(obj -> Utils.gson.toJson(obj,type));
            this.typesByType.put(type, rpcType);
            this.typesByName.put(abstractName,rpcType);
        }
    }
    public void add(Type type, String abstractName, AbstractType.ISerialize serialize, AbstractType.IDeserialize deserialize) throws TrackException {
        if (typesByName.containsKey(abstractName) || typesByType.containsKey(type)) throw new TrackException(TrackException.ErrorCode.Core,String.format("类型:{%s}转{%s}发生异常,存在重复键",type, abstractName));
        else{
            AbstractType rpcType = new AbstractType();
            rpcType.setName(abstractName);
            rpcType.setType(type);
            rpcType.setSerialize(serialize);
            rpcType.setDeserialize(deserialize);
            this.typesByType.put(type, rpcType);
            this.typesByName.put(abstractName,rpcType);
        }
    }
}
