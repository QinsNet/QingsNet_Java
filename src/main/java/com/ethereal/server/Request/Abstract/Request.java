package com.ethereal.server.Request.Abstract;

import com.ethereal.server.Core.Annotation.BaseParam;
import com.ethereal.server.Core.BaseCore.MZCore;
import com.ethereal.server.Core.Manager.AbstractType.AbstractTypeManager;
import com.ethereal.server.Core.Manager.AbstractType.Param;
import com.ethereal.server.Core.Model.*;
import com.ethereal.server.Request.Annotation.RequestMapping;
import com.ethereal.server.Request.Interface.IRequest;
import com.ethereal.server.Service.Abstract.Service;
import com.ethereal.server.Utils.AnnotationUtils;
import com.ethereal.server.Utils.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.ConcurrentHashMap;
@com.ethereal.server.Request.Annotation.Request
public abstract class Request extends MZCore implements IRequest {
    protected final ConcurrentHashMap<Integer,ClientRequestModel> tasks = new ConcurrentHashMap<>();
    protected String name;
    protected Service service;
    protected RequestConfig config;
    protected AbstractTypeManager types = new AbstractTypeManager();

    public AbstractTypeManager getTypes() {
        return types;
    }

    public void setTypes(AbstractTypeManager types) {
        this.types = types;
    }

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

    public RequestConfig getConfig() {
        return config;
    }
    public void setConfig(RequestConfig config) {
        this.config = config;
    }
    public ConcurrentHashMap<Integer, ClientRequestModel> getTasks() {
        return tasks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

}
