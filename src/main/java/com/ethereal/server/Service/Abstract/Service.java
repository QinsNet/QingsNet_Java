package com.ethereal.server.Service.Abstract;

import com.ethereal.server.Core.Event.ExceptionEvent;
import com.ethereal.server.Core.Event.LogEvent;
import com.ethereal.server.Core.Model.AbstractType;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Core.Model.TrackLog;
import com.ethereal.server.Core.Model.AbstractTypes;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Server.Abstract.Token;
import com.ethereal.server.Service.Event.Delegate.InterceptorDelegate;
import com.ethereal.server.Service.Event.InterceptorEvent;
import com.ethereal.server.Service.Interface.IService;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class Service implements IService {
    protected HashMap<String,Method> methods = new HashMap<>();
    protected AbstractTypes types = new AbstractTypes();
    protected String netName;
    protected String name;
    protected ServiceConfig config;
    protected ExceptionEvent exceptionEvent = new ExceptionEvent();
    protected LogEvent logEvent = new LogEvent();
    protected InterceptorEvent interceptorEvent = new InterceptorEvent();

    public String getNetName() {
        return netName;
    }

    public void setNetName(String netName) {
        this.netName = netName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExceptionEvent(ExceptionEvent exceptionEvent) {
        this.exceptionEvent = exceptionEvent;
    }

    public void setLogEvent(LogEvent logEvent) {
        this.logEvent = logEvent;
    }

    public InterceptorEvent getInterceptorEvent() {
        return interceptorEvent;
    }

    public void setInterceptorEvent(InterceptorEvent interceptorEvent) {
        this.interceptorEvent = interceptorEvent;
    }

    public AbstractTypes getTypes() {
        return types;
    }

    public void setTypes(AbstractTypes types) {
        this.types = types;
    }

    public HashMap<String, Method> getMethods() {
        return methods;
    }
    public void setMethods(HashMap<String, Method> methods) {
        this.methods = methods;
    }

    public ServiceConfig getConfig() {
        return config;
    }

    public void setConfig(ServiceConfig config) {
        this.config = config;
    }


    public ExceptionEvent getExceptionEvent() {
        return exceptionEvent;
    }

    public LogEvent getLogEvent() {
        return logEvent;
    }
    @Override

    public void onException(TrackException.ErrorCode code, String message) {
        onException(new TrackException(code,message));
    }
    @Override

    public void onException(TrackException exception){
        exceptionEvent.onEvent(exception);
    }
    @Override

    public void onLog(TrackLog.LogCode code, String message){
        onLog(new TrackLog(code,message));
    }
    @Override

    public void onLog(TrackLog log){
        logEvent.onEvent(log);
    }

    public boolean OnInterceptor(Net net,Method method, Token token)
    {
        if (interceptorEvent != null)
        {
            for (InterceptorDelegate item : interceptorEvent.getListeners())
            {
                if (!item.onInterceptor(net,this, method, token)) return false;
            }
            return true;
        }
        else return true;
    }

    public static void register(Service instance) throws TrackException {
        //反射 获取类信息=>字段、属性、方法
        StringBuilder methodId = new StringBuilder();
        for(Method method : instance.getClass().getMethods())
        {
            int modifier = method.getModifiers();
            com.ethereal.server.Service.Annotation.Service annotation = method.getAnnotation(com.ethereal.server.Service.Annotation.Service.class);
            if(annotation!=null){
                if(!Modifier.isInterface(modifier)){
                    methodId.append(method.getName());
                    Parameter[] parameterInfos = method.getParameters();
                    for(Parameter parameterInfo : parameterInfos){
                        if(parameterInfo.getAnnotation(com.ethereal.server.Server.Annotation.Token.class) != null){
                            continue;
                        }
                        else {
                            AbstractType type = instance.getTypes().getTypesByType().get(parameterInfo.getParameterizedType());
                            if(type == null)type = instance.getTypes().getTypesByName().get(method.getAnnotation(com.ethereal.server.Core.Annotation.AbstractType.class).abstractName());
                            if(type == null)throw new TrackException(TrackException.ErrorCode.Runtime,String.format("RPC中的%s类型参数尚未被注册！",parameterInfo.getParameterizedType()));
                            methodId.append("-").append(type.getName());
                        }
                    }
                    instance.methods.put(methodId.toString(),method);
                    methodId.setLength(0);
                }
            }
        }
    }
}
