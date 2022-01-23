package com.ethereal.net.request.core;

import com.ethereal.net.core.annotation.BaseParam;
import com.ethereal.net.core.base.BaseCore;
import com.ethereal.net.core.manager.ioc.IocManager;
import com.ethereal.net.core.manager.type.AbstractTypeManager;
import com.ethereal.net.core.manager.type.Param;
import com.ethereal.net.core.entity.*;
import com.ethereal.net.node.core.Node;
import com.ethereal.net.request.annotation.RequestMapping;
import com.ethereal.net.service.core.Service;
import com.ethereal.net.service.event.InterceptorEvent;
import com.ethereal.net.utils.AnnotationUtils;
import lombok.Getter;
import lombok.Setter;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


@Getter
@Setter
@com.ethereal.net.request.annotation.Request
public abstract class Request extends BaseCore implements IRequest {
    protected final ConcurrentHashMap<Integer, RequestMeta> tasks = new ConcurrentHashMap<>();
    protected InterceptorEvent interceptorEvent = new InterceptorEvent();
    protected HashMap<String,Request> requests = new HashMap<>();
    protected String name;
    protected String prefixes;
    protected Request parent;
    protected RequestConfig config;
    protected Boolean initialized = false;
    protected Node node;
    protected AbstractTypeManager types = new AbstractTypeManager();
    protected IocManager iocManager = new IocManager();

    public Request() throws TrackException {
        for (Method method : getClass().getMethods()){
            RequestMapping requestAnnotation = method.getAnnotation(RequestMapping.class);
            if(requestAnnotation !=null){
                for (Parameter parameter : method.getParameters()){
                    if(AnnotationUtils.getAnnotation(parameter,BaseParam.class) != null){
                        continue;
                    }
                    Param paramAnnotation = method.getAnnotation(Param.class);
                    if(paramAnnotation != null){
                        String typeName = paramAnnotation.type();
                        if(getTypes().get(typeName) == null){
                            throw new TrackException(TrackException.ErrorCode.Initialize, String.format("%s-%s-%s抽象类型未找到",getName() ,method.getName(),paramAnnotation.type()));
                        }
                    }
                    else if(getTypes().get(parameter.getParameterizedType()) == null){
                        throw new TrackException(TrackException.ErrorCode.Initialize, String.format("%s-%s-%s类型映射抽象类型",getName() ,method.getName(),parameter.getParameterizedType()));
                    }
                }
            }
        }
    }

    public <T> T register(Class<?> requestClass) throws TrackException {
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
        if(!requests.containsKey(request.name)){
            request.setInitialized(true);
            request.initialize();
            request.setParent(this);
            request.setPrefixes(parent.getPrefixes() + "/" + name);
            request.getExceptionEvent().register(request::onException);
            request.getLogEvent().register(request::onLog);
            requests.put(request.getName(), request);
            return (T)request;
        }
        else throw new TrackException(TrackException.ErrorCode.Initialize,String.format("%s/%s已注册,无法重复注册！", prefixes,request.name));
    }


    public boolean unRegister() throws TrackException {
        if(initialized){
            unInitialize();
            for(Request request : requests.values()){
                request.unRegister();
            }
            getExceptionEvent().clear();
            getLogEvent().clear();
            parent.getRequests().remove(name);
            parent = null;
            prefixes = null;
            initialized = false;
            return true;
        }
        else throw new TrackException(TrackException.ErrorCode.Runtime, String.format("%s已经注销,无法重复注销",prefixes));
    }
}
