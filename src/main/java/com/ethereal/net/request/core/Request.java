package com.ethereal.net.request.core;

import com.ethereal.net.core.annotation.BaseParam;
import com.ethereal.net.core.base.MZCore;
import com.ethereal.net.core.manager.type.Param;
import com.ethereal.net.core.entity.*;
import com.ethereal.net.request.annotation.RequestMapping;
import com.ethereal.net.service.core.Service;
import com.ethereal.net.utils.AnnotationUtils;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.ConcurrentHashMap;


@Getter
@Setter
@com.ethereal.net.request.annotation.Request
public abstract class Request extends MZCore implements IRequest {
    protected final ConcurrentHashMap<Integer, RequestMeta> tasks = new ConcurrentHashMap<>();
    protected String name;
    protected Service service;
    protected RequestConfig config;

    public static void register(Request instance) throws TrackException {
        for (Method method : instance.getClass().getMethods()){
            RequestMapping requestAnnotation = method.getAnnotation(RequestMapping.class);
            if(requestAnnotation !=null){
                for (Parameter parameter : method.getParameters()){
                    if(AnnotationUtils.getAnnotation(parameter,BaseParam.class) != null){
                        continue;
                    }
                    Param paramAnnotation = method.getAnnotation(Param.class);
                    if(paramAnnotation != null){
                        String typeName = paramAnnotation.type();
                        if(instance.getTypes().get(typeName) == null){
                            throw new TrackException(TrackException.ErrorCode.Core, String.format("%s-%s-%s抽象类型未找到",instance.getName() ,method.getName(),paramAnnotation.type()));
                        }
                    }
                    else if(instance.getTypes().get(parameter.getParameterizedType()) == null){
                        throw new TrackException(TrackException.ErrorCode.Core, String.format("%s-%s-%s类型映射抽象类型",instance.getName() ,method.getName(),parameter.getParameterizedType()));
                    }
                }
            }
        }
    }


}
