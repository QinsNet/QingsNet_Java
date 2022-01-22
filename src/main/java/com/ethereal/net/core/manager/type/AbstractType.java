package com.ethereal.net.core.manager.type;

import java.lang.reflect.Type;

public class AbstractType {
    public interface IDeserialize {
        Object Deserialize(String obj);
    }
    public interface ISerialize {
        String  Serialize(Object obj);
    }

    private IDeserialize deserialize;
    private ISerialize serialize;
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
}
