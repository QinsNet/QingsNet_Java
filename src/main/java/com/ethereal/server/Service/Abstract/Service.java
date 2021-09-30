package com.ethereal.server.Service.Abstract;

import com.ethereal.server.Core.Event.ExceptionEvent;
import com.ethereal.server.Core.Event.LogEvent;
import com.ethereal.server.Core.Model.AbstractType;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Core.Model.TrackLog;
import com.ethereal.server.Core.Model.AbstractTypes;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Request.Abstract.Request;
import com.ethereal.server.Server.Abstract.BaseToken;
import com.ethereal.server.Service.Event.Delegate.InterceptorDelegate;
import com.ethereal.server.Service.Event.InterceptorEvent;
import com.ethereal.server.Service.Interface.IService;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;

public abstract class Service implements IService {
    protected HashMap<String,Method> methods = new HashMap<>();
    protected AbstractTypes types;
    protected String netName;
    protected String name;
    protected ServiceConfig config;
    protected ExceptionEvent exceptionEvent = new ExceptionEvent();
    protected LogEvent logEvent = new LogEvent();
    protected InterceptorEvent interceptorEvent = new InterceptorEvent();
    public Service(String name,AbstractTypes types){
        this.name = name;
        this.types = types;
    }
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

    public boolean OnInterceptor(Net net,Method method, BaseToken token)
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

    public static void register(Service instance, String netName) throws Exception {
        instance.netName = netName;
        //反射 获取类信息=>字段、属性、方法
        StringBuilder methodId = new StringBuilder();
        for(Method method : instance.getClass().getMethods())
        {
            int modifier = method.getModifiers();
            com.ethereal.server.Service.Annotation.Service annotation = method.getAnnotation(com.ethereal.server.Service.Annotation.Service.class);
            if(annotation!=null){
                if(!Modifier.isInterface(modifier)){
                    methodId.append(method.getName());
                    int startIdx = 1;
                    if(annotation.parameters().length == 0){
                        Class<?>[] parameters = method.getParameterTypes();
                        if(parameters.length > 0 && !BaseToken.class.isAssignableFrom(parameters[0])){
                            startIdx = 0;
                        }
                        for(int i=startIdx;i<parameters.length;i++){
                            AbstractType rpcType = instance.types.getTypesByType().get(parameters[i]);
                            if(rpcType != null) {
                                methodId.append("-").append(rpcType.getName());
                            }
                            else throw new TrackException(TrackException.ErrorCode.Runtime,String.format("Java中的%s类型参数尚未注册,请注意是否是泛型导致！",parameters[i].getName()));
                        }
                    }
                    else {
                        String[] types_name = annotation.parameters();
                        for(String type_name : types_name){
                            if(instance.types.getTypesByName().containsKey(type_name)){
                                methodId.append("-").append(type_name);
                            }
                            else throw new TrackException(TrackException.ErrorCode.Runtime,String.format("Java中的%s抽象类型参数尚未注册,请注意是否是泛型导致！",type_name));
                        }
                    }
                    instance.methods.put(methodId.toString(),method);
                    methodId.setLength(0);
                }
            }
        }
    }
}
