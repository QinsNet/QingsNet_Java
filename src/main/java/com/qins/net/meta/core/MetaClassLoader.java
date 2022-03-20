package com.qins.net.meta.core;

import com.qins.net.meta.annotation.Components;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.annotation.Meta;
import com.qins.net.meta.util.PackageScanner;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.HashMap;

@Getter
public class MetaClassLoader{
    PackageScanner scanner;
    private final HashMap<String,BaseClass> bases = new HashMap<>();
    private final HashMap<Type,BaseClass> types = new HashMap<>();
    private final HashMap<String,MetaClass> metas = new HashMap<>();
    public MetaClassLoader(PackageScanner scanner){
        this.scanner = scanner;
    }
    public BaseClass loadClass(String name,Class<?> instanceClass) throws LoadClassException {
        try {
            if(types.containsKey(instanceClass)){
                return types.get(instanceClass);
            }
            else if(bases.containsKey(name)){
                return bases.get(name);
            }
            Components components = instanceClass.getAnnotation(Components.class) != null ? instanceClass.getAnnotation(Components.class) : Components.class.getAnnotation(Components.class);
            BaseClass baseClass;
            if(instanceClass.getAnnotation(Meta.class) != null){
                baseClass = components.metaClass().getConstructor(String.class,Class.class).newInstance(name,instanceClass);
                metas.put(name, (MetaClass) baseClass);
                types.put(((MetaClass) baseClass).getProxyClass(),baseClass);
            }
            else {
                if(instanceClass == Boolean.class || instanceClass == Character.class || instanceClass == Byte.class
                        || instanceClass == Short.class || instanceClass == Integer.class || instanceClass == Long.class
                        || instanceClass == Float.class || instanceClass == Double.class || instanceClass == Void.class
                        || instanceClass == String.class || instanceClass.isPrimitive()){
                    baseClass = components.primitiveClass().getConstructor(String.class,Class.class).newInstance(name,instanceClass);
                }
                else baseClass = components.referenceClass().getConstructor(String.class,Class.class).newInstance(name,instanceClass);
                types.put(baseClass.getInstanceClass(),baseClass);
            }
            bases.put(name, baseClass);
            baseClass.link();
            return baseClass;
        }
        catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new LoadClassException(e);
        }
    }
    public BaseClass loadClass(Class<?> instanceClass) throws LoadClassException {
        if(types.containsKey(instanceClass)){
            return types.get(instanceClass);
        }
        Meta meta = instanceClass.getAnnotation(Meta.class);
        String name = meta == null || "".equals(meta.value()) ? instanceClass.getName() : meta.value();
        return loadClass(name,instanceClass);
    }
    public BaseClass loadClass(String name) throws LoadClassException, ClassNotFoundException {
        if(bases.containsKey(name)){
            return bases.get(name);
        }
        //从默认类加载器中找类
        try {
            Class<?> klass = Thread.currentThread().getContextClassLoader().loadClass(name);
            return loadClass(name,klass);
        } catch (ClassNotFoundException e) {
            //扫包
            Class<?> klass = scanner.packageScan(name);
            if(klass == null)throw new ClassNotFoundException(name);
            return loadClass(name,klass);
        }
    }
}
