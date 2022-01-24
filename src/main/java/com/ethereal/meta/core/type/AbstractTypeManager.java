package com.ethereal.meta.core.type;
import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.utils.Utils;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;

public class AbstractTypeManager {
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

    public AbstractTypeManager(){

    }
    public void add(Type type, String abstractName) throws TrackException {
        if (typesByName.containsKey(abstractName) || typesByType.containsKey(type)) throw new TrackException(TrackException.ErrorCode.Initialize,String.format("类型:{%s}转{%s}发生异常,存在重复键",type, abstractName));
        else{
            AbstractType rpcType = new AbstractType();
            rpcType.setName(abstractName);
            rpcType.setType(type);
            rpcType.setDeserialize(obj -> Utils.gson.fromJson(obj,type));
            rpcType.setSerialize(obj -> Utils.gson.toJson(obj,type));
            if(!typesByType.containsKey(type))this.typesByType.put(type, rpcType);
            this.typesByName.put(abstractName,rpcType);
        }
    }
    public void add(Type type, String abstractName, AbstractType.ISerialize serialize, AbstractType.IDeserialize deserialize) throws TrackException {
        if (typesByName.containsKey(abstractName) || typesByType.containsKey(type)) throw new TrackException(TrackException.ErrorCode.Initialize,String.format("类型:{%s}转{%s}发生异常,存在重复键",type, abstractName));
        else{
            AbstractType rpcType = new AbstractType();
            rpcType.setName(abstractName);
            rpcType.setType(type);
            rpcType.setSerialize(serialize);
            rpcType.setDeserialize(deserialize);
            if(!typesByType.containsKey(type))this.typesByType.put(type, rpcType);
            this.typesByName.put(abstractName,rpcType);
        }
    }

    public AbstractType get(String name)
    {
        return typesByName.get(name);
    }

    public AbstractType get(Type type)
    {
        return typesByType.get(type);
    }

    public AbstractType get(Parameter parameterInfo)
    {
        Param paramAttribute = parameterInfo.getAnnotation(Param.class);
        if (paramAttribute != null)
        {
            return typesByName.get(paramAttribute.type());
        }
        return typesByType.get(parameterInfo.getParameterizedType());
    }
    public AbstractType get(String name, Type type)
    {
        if (name != null)
        {
            return typesByName.get(name);
        }
        return typesByType.get(type);
    }
}
