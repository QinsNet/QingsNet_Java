package com.ethereal.meta.request.core;

import com.ethereal.meta.core.entity.*;
import com.ethereal.meta.net.core.Net;
import com.ethereal.meta.request.annotation.*;
import com.ethereal.meta.util.AnnotationUtil;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;



@RequestAnnotation
public abstract class Request extends Net implements IRequest {
    @Getter
    protected final HashMap<String, Method> requests = new HashMap<>();
    @Getter
    protected final ConcurrentHashMap<String,RequestMeta> tasks = new ConcurrentHashMap<>();
    @Getter
    protected RequestConfig requestConfig;
    public void receive(ResponseMeta responseMeta) {
        try {
            if(!tasks.containsKey(responseMeta.getId())){
                throw new TrackException(TrackException.ExceptionCode.NotFoundRequest, String.format("请求:%s ID：%s 未找到",prefixes,responseMeta.getId()));
            }
            RequestMeta requestMeta = tasks.remove(responseMeta.getId());
            synchronized (requestMeta){
                requestMeta.setResult(responseMeta);
                requestMeta.notify();
            }
        }
        catch (Exception e){
            onException(e);
        }
    }

    protected Request(){
        for (Method method : this.getClass().getMethods()){
            if(AnnotationUtil.getAnnotation(method,RequestAnnotation.class) != null){
                RequestMapping requestMapping = getRequestMapping(method);
                requests.put(requestMapping.getMapping(),method);
            }
        }
    }

    public RequestMapping getRequestMapping(Method method){
        RequestMapping requestMapping = new RequestMapping();
        if(method.getAnnotation(PostRequest.class) != null){
            PostRequest annotation = method.getAnnotation(PostRequest.class);
            requestMapping.setMapping(annotation.mapping());
            requestMapping.setInvoke(annotation.invoke());
            requestMapping.setTimeout(annotation.timeout());
            requestMapping.setMethod(RequestType.Post);
        }
        else if(method.getAnnotation(GetRequest.class) != null){
            GetRequest annotation = method.getAnnotation(GetRequest.class);
            requestMapping.setMapping(annotation.mapping());
            requestMapping.setInvoke(annotation.invoke());
            requestMapping.setTimeout(annotation.timeout());
            requestMapping.setMethod(RequestType.Get);
        }
        return requestMapping;
    }
}
