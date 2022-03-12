package com.qins.net.meta.core;

import com.qins.net.meta.annotation.Components;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.annotation.Meta;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

@Getter
public class MetaClassLoader extends ClassLoader{

    private final HashMap<String,BaseClass> bases = new HashMap<>();
    private final HashMap<String,MetaClass> metas = new HashMap<>();

    public BaseClass loadClass(Class<?> instanceClass) throws LoadClassException {
        try {
            Meta meta = instanceClass.getAnnotation(Meta.class);
            String name = meta == null || "".equals(meta.value()) ? instanceClass.getName() : meta.value();
            if(bases.containsKey(name))return bases.get(name);
            else {
                Components components = instanceClass.getAnnotation(Components.class) != null ? instanceClass.getAnnotation(Components.class) : Components.class.getAnnotation(Components.class);
                BaseClass baseClass;
                if(meta != null){
                    baseClass = components.metaClass().getConstructor(Class.class).newInstance(instanceClass);
                    metas.put(name, (MetaClass) baseClass);
                }
                else {
                    baseClass = components.baseClass().getConstructor(Class.class).newInstance(instanceClass);
                }
                bases.put(name, baseClass);
                baseClass.link();
                return baseClass;
            }
        }
        catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new LoadClassException(e);
        }
    }
}
