package com.ethereal.net.node.core;

import com.ethereal.net.core.base.BaseCore;
import com.ethereal.net.core.entity.TrackException;
import com.ethereal.net.node.event.NodeStartEvent;
import com.ethereal.net.node.event.NodeCloseEvent;
import com.ethereal.net.node.network.INetwork;
import com.ethereal.net.request.annotation.RequestMapping;
import com.ethereal.net.request.core.Request;
import com.ethereal.net.request.core.RequestInterceptor;
import com.ethereal.net.service.core.Service;
import lombok.Getter;
import lombok.Setter;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import java.util.HashMap;

@Getter
public abstract class Node extends BaseCore implements INode {
    protected NodeConfig config;
    @Setter
    protected INetwork network;
    protected NodeStartEvent startEvent = new NodeStartEvent();
    protected NodeCloseEvent closeEvent = new NodeCloseEvent();

    public void onStart(){
        startEvent.onEvent(this);
    }
    public void onClose(){
        closeEvent.onEvent(this);
    }
    public <T> T register(Class<? extends Request> requestClass) throws TrackException {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(requestClass);
        RequestInterceptor interceptor = new RequestInterceptor();
        Callback noOp= NoOp.INSTANCE;
        enhancer.setCallbacks(new Callback[]{noOp,interceptor});
        enhancer.setCallbackFilter(method -> {
            if(method.getAnnotation(RequestMapping.class) != null){
                return 1;
            }
            else return 0;
        });
        Request request = (Request)enhancer.create();
        request.setInitialized(true);
        request.initialize();
        request.setPrefixes(request.getName());
        return (T)request;
    }
    public  <T> T register(Service service) throws TrackException {
        service.setInitialized(true);
        service.initialize();
        service.setPrefixes(service.getName());
        return (T) service;
    }
}
