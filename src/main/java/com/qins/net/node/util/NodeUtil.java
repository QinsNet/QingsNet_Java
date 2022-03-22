package com.qins.net.node.util;

import com.qins.net.core.exception.TrackException;
import com.qins.net.request.cglib.RequestInterceptor;
import net.sf.cglib.proxy.Factory;

import java.util.HashMap;
import java.util.Map;

public class NodeUtil {
    public static Map<String,String> getNodes(Object instance) throws TrackException {
        if(instance == null)throw new NullPointerException();
        if(instance instanceof Factory){
            RequestInterceptor interceptor = (RequestInterceptor) ((Factory) instance).getCallback(1);
            return interceptor.getNodes();
        }
        else throw new TrackException(TrackException.ExceptionCode.NotMetaClass,instance.getClass().getName() + "未标记@Sync");
    }
    public static boolean defineNode(Object instance,String mapping,String address) throws TrackException {
        Map<String,String> nodes = getNodes(instance);
        nodes.remove(mapping);
        nodes.put(mapping,address);
        return true;
    }
    public static void copyNodeIfAbsent(Object origin,Object target,String name) throws TrackException {
        Map<String,String> originNodes = getNodes(origin);
        Map<String,String> targetNodes = getNodes(target);
        if(originNodes.containsKey(name)){
            targetNodes.putIfAbsent(name,originNodes.get(name));
        }
    }
    public static void copyNode(Object origin,Object target,String name) throws TrackException {
        Map<String,String> originNodes = getNodes(origin);
        Map<String,String> targetNodes = getNodes(target);
        if(originNodes.containsKey(name)){
            targetNodes.remove(name);
            targetNodes.put(name,originNodes.get(name));
        }
    }
    public static boolean replaceNode(Object origin,Object target,String name) throws TrackException {
        Map<String,String> originNodes = getNodes(origin);
        Map<String,String> targetNodes = getNodes(target);
        if(originNodes.containsKey(name)){
            targetNodes.remove(name);
            targetNodes.put(name,originNodes.get(name));
            return true;
        }
        else return false;
    }
    public static void copyNodeAll(Object origin,Object target) throws TrackException {
        Map<String,String> originNodes = getNodes(origin);
        Map<String,String> targetNodes = getNodes(target);
        targetNodes.putAll(originNodes);
    }

    public static void replaceNodeAll(Object origin,Object target) throws TrackException {
        Map<String,String> originNodes = getNodes(origin);
        Map<String,String> targetNodes = getNodes(target);
        for (Map.Entry<String,String> node : originNodes.entrySet()){
            targetNodes.putIfAbsent(node.getKey(),node.getValue());
        }
    }
}
