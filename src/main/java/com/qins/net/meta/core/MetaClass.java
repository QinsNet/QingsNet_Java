package com.qins.net.meta.core;

import com.qins.net.core.aop.EventManager;
import com.qins.net.core.entity.NodeAddress;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.instance.InstanceManager;
import com.qins.net.meta.annotation.Meta;
import com.qins.net.request.core.Request;
import com.qins.net.request.core.RequestInterceptor;
import com.qins.net.service.core.Service;
import lombok.Getter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;

@Getter
public abstract class MetaClass extends BaseClass {
    protected Request request;
    protected Service service;
    protected HashMap<String, MetaField> metas = new HashMap<>();
    protected EventManager eventManager = new EventManager();
    protected InstanceManager instanceManager = new InstanceManager();
    protected String name;
    protected Class<?> proxyClass;

    @Override
    protected void onLink() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super.onLink();
        for (MetaField metaField : fields.values()){
            Meta meta = metaField.getField().getType().getAnnotation(Meta.class);
            if(meta != null){
                metas.put(metaField.name,metaField);
            }
        }
    }

    public void updateNode(Object instance, NodeAddress local, NodeAddress remote)  {
        if(instance == null)return;
        try {
            if(Iterable.class.isAssignableFrom(instance.getClass())){
                for (Object item:(Iterable<Factory>) instance){
                    updateNode(item,local,remote);
                }
            }
            else if(Map.class.isAssignableFrom(instance.getClass())){
                for(Map.Entry<Object,Object> item:((Map<Object,Object>)instance).entrySet()){
                    if(item.getKey().getClass().isAssignableFrom(Factory.class)){
                        updateNode(item.getKey(), local,remote);
                    }
                    else if(item.getValue().getClass().isAssignableFrom(Factory.class)){
                        updateNode(item.getValue(), local,remote);
                    }
                }
            }
            else {
                Factory factory = (Factory) instance;
                factory.setCallback(0, NoOp.INSTANCE);
                factory.setCallback(1,new RequestInterceptor(request,local,remote));
            }
            //子层
            for(MetaField metaField : metas.values()){
                Object value = metaField.getField().get(instance);
                if(value != null) {
                    ((MetaClass)(metaField.baseClass)).updateNode(value,local,remote);
                }
            }
        }
        catch (Exception e){
            onException(e);
        }
    }

    public MetaClass(Class<?> instanceClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(instanceClass);
        Meta meta = instanceClass.getAnnotation(Meta.class);
        if(meta != null){
            name = "".equals(meta.value()) ? instanceClass.getSimpleName() : meta.value();
            //Life Cycle
            service = components.service().getConstructor(MetaClass.class).newInstance(this);
            request = components.request().getConstructor(MetaClass.class).newInstance(this);

            //Proxy Instance
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(instanceClass);
            enhancer.setCallbackTypes(new Class[]{NoOp.class,RequestInterceptor.class});
            enhancer.setCallbackFilter(method ->
            {
                if(method.getAnnotation(Meta.class) == null)return 0;
                return 1;
            });
            proxyClass = enhancer.createClass();
            //Life Cycle
            onLink();
        }
    }
    public <T> T newInstance(NodeAddress local, NodeAddress remote) throws NewInstanceException {
        try {
            Object instance = proxyClass.newInstance();
            updateNode(instance,local,remote);
            return (T) instance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new NewInstanceException(e.getCause());
        }
    }
    public <T> T newInstance(String rawInstance,NodeAddress local, NodeAddress remote) {
        Factory instance = (Factory) deserialize(rawInstance);
        if(instance == null)return null;
        updateNode(instance,local,remote);
        return (T)instance;
    }
}
