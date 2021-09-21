package Old.Model;

import java.lang.reflect.Type;

/**
 * 中间层抽象数据类
 */
public class RPCType {
   public interface IDeserialize{
       Object Deserialiae(String obj);
   }
   public interface  ISerialize{
       String Serialize(Object obj);
   }

   private IDeserialize deserialize;
   private  ISerialize serialize;
   private Type type;
   private String name;

    public IDeserialize getDeserialize() {
        return deserialize;
    }

    public void setDeserialize(IDeserialize deserialize) {
        this.deserialize = deserialize;
    }

    public ISerialize getSerialize() {
        return serialize;
    }

    public void setSerialize(ISerialize serialize) {
        this.serialize = serialize;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RPCType() {
    }

    public RPCType(Type type, String name) {
        this.type = type;
        this.name = name;
    }
}
