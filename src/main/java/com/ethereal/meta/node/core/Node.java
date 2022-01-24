package com.ethereal.meta.node.core;

import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.node.event.NodeStartEvent;
import com.ethereal.meta.node.event.NodeCloseEvent;
import com.ethereal.meta.node.network.INetwork;
import com.ethereal.meta.service.core.Service;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Node extends Service implements INode {
    protected NodeConfig nodeConfig;
    @Setter
    protected INetwork network;
    protected NodeStartEvent startEvent = new NodeStartEvent();
    protected NodeCloseEvent closeEvent = new NodeCloseEvent();

    public Node() throws TrackException {

    }

    public void onStart(){
        startEvent.onEvent(this);
    }
    public void onClose(){
        closeEvent.onEvent(this);
    }

//    public <T> T register(Class<? extends RequestMeta> requestClass) throws TrackException {
//        Enhancer enhancer = new Enhancer();
//        enhancer.setSuperclass(requestClass);
//        RequestInterceptor interceptor = new RequestInterceptor();
//        Callback noOp= NoOp.INSTANCE;
//        enhancer.setCallbacks(new Callback[]{noOp,interceptor});
//        enhancer.setCallbackFilter(method -> {
//            if(method.getAnnotation(RequestMapping.class) != null){
//                return 1;
//            }
//            else return 0;
//        });
//        RequestMeta request = (RequestMeta)enhancer.create();
//        request.setInitialized(true);
//        request.initialize();
//        request.setPrefixes(request.getName());
//        return (T)request;
//    }
//    public  <T> T register(ServiceMeta serviceNet) throws TrackException {
//        serviceNet.setInitialized(true);
//        serviceNet.initialize();
//        serviceNet.setPrefixes(serviceNet.getName());
//        return (T) serviceNet;
//    }
}
