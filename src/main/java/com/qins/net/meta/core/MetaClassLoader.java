package com.qins.net.meta.core;

import com.qins.net.meta.annotation.Components;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.annotation.Meta;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;

@Getter
public class MetaClassLoader extends ClassLoader{

    private final HashMap<String,BaseClass> bases = new HashMap<>();
    private final HashMap<String,MetaClass> metas = new HashMap<>();

    public BaseClass loadClass(String typeName, Class<?> instanceClass) throws LoadClassException {
        try {
            if(bases.containsKey(typeName))return bases.get(typeName);
            else {
                Components components = instanceClass.getAnnotation(Components.class) != null ? instanceClass.getAnnotation(Components.class) : Components.class.getAnnotation(Components.class);
                BaseClass baseClass;
                Meta meta = instanceClass.getAnnotation(Meta.class);
                if(meta != null){
                    baseClass = loadMetaClass(instanceClass);
                }
                else {
                    baseClass = components.baseClass().getConstructor(Class.class).newInstance(instanceClass);
                }
                bases.put(typeName, baseClass);
                return baseClass;
            }
        }
        catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new LoadClassException(e);
        }
    }
    public BaseClass loadMetaClass(Class<?> instanceClass) throws LoadClassException {
        try {
            Meta meta = instanceClass.getAnnotation(Meta.class);
            if(meta != null){
                String name = "".equals(meta.name()) ? instanceClass.getSimpleName() : meta.name();
                if(metas.containsKey(name))return metas.get(name);
                Components components = instanceClass.getAnnotation(Components.class) != null ? instanceClass.getAnnotation(Components.class) : Components.class.getAnnotation(Components.class);
                BaseClass baseClass = components.metaClass().getConstructor(Class.class).newInstance(instanceClass);
                metas.put(name, (MetaClass) baseClass);
                return baseClass;
            }
            else throw new LoadClassException(String.format("%s 未定义@Meta", instanceClass.getName()));
        }
        catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new LoadClassException(e);
        }
    }
}
