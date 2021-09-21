package Old.Model;

import Model.RPCException;
import Model.RPCType;
import Utils.Utils;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Objects;

/**
 * RPCType配置项
 */
public class RPCTypeConfig {
    //从Type到RPCType的映射表
    public interface IConvert{
        Object convert(Object obj);
    }
    //根据类型进行映射
    private HashMap<Type, Model.RPCType> typesByType =new HashMap<>();
    //根据名字进行映射
    private HashMap<String, Model.RPCType> typesByName = new HashMap<>();
    public HashMap<Type, Model.RPCType> getTypesByType() {
        return typesByType;
    }

    public void setTypesByType(HashMap<Type, Model.RPCType> typesByType) {
        this.typesByType = typesByType;
    }

    public HashMap<String, Model.RPCType> getTypesByName() {
        return typesByName;
    }

    public void setTypesByName(HashMap<String, Model.RPCType> typesByName) {
        this.typesByName = typesByName;
    }

    public RPCTypeConfig(){

    }
    //通过类型和类型名增加抽象类型
    public void add(Type type, String abstractName) throws Model.RPCException {
        if (typesByName.containsKey(abstractName) || typesByType.containsKey(type)) throw new Model.RPCException(Model.RPCException.ErrorCode.Core,String.format("类型:{%s}转{%s}发生异常,存在重复键",type, abstractName));
        else{
            Model.RPCType rpcType = new Model.RPCType();
            rpcType.setName(abstractName);
            rpcType.setType(type);
            rpcType.setDeserialize(obj -> Utils.gson.fromJson(obj,type));
            rpcType.setSerialize(obj -> Utils.gson.toJson(obj,type));
            this.typesByType.put(type, rpcType);
            this.typesByName.put(abstractName,rpcType);
        }
    }
    //之前add方法的重载增加了serialize和deserialize不需要向之前一样通过gson去序列化
    public void add(Type type, String abstractName, Model.RPCType.ISerialize serialize, Model.RPCType.IDeserialize deserialize) throws Model.RPCException {
        if (typesByName.containsKey(abstractName) || typesByType.containsKey(type)) throw new Model.RPCException(RPCException.ErrorCode.Core,String.format("类型:{%s}转{%s}发生异常,存在重复键",type, abstractName));
        else{
            Model.RPCType rpcType = new RPCType();
            rpcType.setName(abstractName);
            rpcType.setType(type);
            rpcType.setSerialize(serialize);
            rpcType.setDeserialize(deserialize);
            this.typesByType.put(type, rpcType);
            this.typesByName.put(abstractName,rpcType);
        }
    }
}




