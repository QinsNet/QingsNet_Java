package com.qins.net.core.type;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;

@Getter
@Setter
public class AbstractType {
    public interface IDeserialize {
        Object deserialize(String obj,Type type);
    }
    public interface ISerialize {
        String serialize(Object obj,Type type);
    }

    private IDeserialize deserialize;
    private ISerialize serialize;
    private Type type;
    private String name;

    public String serialize(Object obj){
        return serialize.serialize(obj,type);
    }
    public Object deserialize(String obj){
        return deserialize.deserialize(obj,type);
    }
}
