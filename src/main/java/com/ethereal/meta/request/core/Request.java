package com.ethereal.meta.request.core;

import com.ethereal.meta.core.annotation.BaseParam;
import com.ethereal.meta.core.type.Param;
import com.ethereal.meta.core.entity.*;
import com.ethereal.meta.meta.RawMeta;
import com.ethereal.meta.request.annotation.RequestMapping;
import com.ethereal.meta.utils.AnnotationUtils;
import lombok.Getter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.ConcurrentHashMap;



@com.ethereal.meta.request.annotation.Request
public abstract class Request extends RawMeta implements IRequest {
    @Getter
    protected final ConcurrentHashMap<Integer, com.ethereal.meta.core.entity.RequestMeta> tasks = new ConcurrentHashMap<>();
    @Getter
    protected RequestConfig requestConfig;
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
                            throw new TrackException(TrackException.ErrorCode.Initialize, String.format("%s-%s-%s抽象类型未找到",getClass().getName() ,method.getName(),paramAnnotation.type()));
                        }
                    }
                    else if(getTypes().get(parameter.getParameterizedType()) == null){
                        throw new TrackException(TrackException.ErrorCode.Initialize, String.format("%s-%s-%s类型映射抽象类型",getClass().getName() ,method.getName(),parameter.getParameterizedType()));
                    }
                }
            }
        }
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
//        if(!requests.containsKey(request.name)){
//            request.setInitialized(true);
//            request.initialize();
//            request.setParent(this);
//            request.setPrefixes(parent.getPrefixes() + "/" + name);
//            request.getExceptionEvent().register(request::onException);
//            request.getLogEvent().register(request::onLog);
//            requests.put(request.getName(), request);
//            return (T)request;
//        }
//        else throw new TrackException(TrackException.ErrorCode.Initialize,String.format("%s/%s已注册,无法重复注册！", prefixes,request.name));
//    }
//
//
//    public boolean unRegister() throws TrackException {
//        if(initialized){
//            unInitialize();
//            for(RequestMeta request : requests.values()){
//                request.unRegister();
//            }
//            getExceptionEvent().clear();
//            getLogEvent().clear();
//            parent.getRequests().remove(name);
//            parent = null;
//            prefixes = null;
//            initialized = false;
//            return true;
//        }
//        else throw new TrackException(TrackException.ErrorCode.Runtime, String.format("%s已经注销,无法重复注销",prefixes));
//    }
}
