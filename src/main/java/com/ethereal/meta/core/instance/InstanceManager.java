package com.ethereal.meta.core.instance;

import com.ethereal.meta.core.entity.TrackException;

import java.util.HashMap;

public class InstanceManager {
    private HashMap<String,Object> iocContainer = new HashMap<>();

    public void register(String name, Object instance) throws TrackException {
        if(iocContainer.containsKey(name)){
            throw new TrackException(TrackException.ExceptionCode.Runtime, String.format("已经注册%sIOC实例",name));
        }
        iocContainer.put(name,instance);
    }

    public void unregister(String name) {
        if(iocContainer.containsKey(name)){
            Object instance = iocContainer.get(name);
            iocContainer.remove(name);
        }
    }

    public Object get(String name) {
        return iocContainer.get(name);
    }

}
