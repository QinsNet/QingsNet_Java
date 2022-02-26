package com.qins.net.meta.core;

import com.qins.net.meta.annotation.Components;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.annotation.Meta;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class MetaClassLoader extends ClassLoader{
    @Getter
    private final HashMap<String,String> nodes = new HashMap<>();
    private final HashMap<String,MetaClass> metas = new HashMap<>();
    private final HashMap<Class<?>,BaseClass> bases = new HashMap<>();
    public BaseClass loadClass(Class<?> instanceClass) throws LoadClassException {
        try {
            Meta meta = instanceClass.getAnnotation(Meta.class);
            if(meta == null){
                if(bases.containsKey(instanceClass))return bases.get(instanceClass);
                else {
                    Components components = Components.class.getAnnotation(Components.class);
                    BaseClass baseClass = components.baseClass().getConstructor(Class.class).newInstance(instanceClass);
                    bases.put(instanceClass,baseClass);
                    return baseClass;
                }
            }
            else return loadMetaClass(meta,instanceClass);
        }
        catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new LoadClassException(e);
        }
    }

    public MetaClass loadMetaClass(Meta meta, Class<?> instanceClass) throws LoadClassException {
        try {
            String name = "".equals(meta.name()) ? instanceClass.getSimpleName() : meta.name();
            if(metas.containsKey(name))return metas.get(name);
            else {
                Components components = instanceClass.getAnnotation(Components.class) != null ? instanceClass.getAnnotation(Components.class) : Components.class.getAnnotation(Components.class);
                MetaClass metaClass = components.metaClass().getConstructor(Class.class).newInstance(instanceClass);
                metas.put(name,metaClass);
                for (Map.Entry<String,String> node : nodes.entrySet()){
                    metaClass.getNodes().putIfAbsent(node.getKey(),node.getValue());
                }
                return metaClass;
            }
        }
        catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new LoadClassException(e);
        }
    }

    public MetaClass getMetaClass(String name) {
        return metas.get(name);
    }

    public BaseClass getBaseClass(Class<?> instanceClass) {
        return bases.get(instanceClass);
    }
}
