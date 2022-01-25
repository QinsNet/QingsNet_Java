package com.ethereal.meta.meta;

import com.ethereal.meta.core.aop.EventManager;
import com.ethereal.meta.core.type.Param;
import com.ethereal.meta.meta.event.ExceptionEvent;
import com.ethereal.meta.meta.event.LogEvent;
import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.core.entity.TrackLog;
import com.ethereal.meta.core.instance.InstanceManager;
import com.ethereal.meta.core.type.AbstractTypeManager;
import com.ethereal.meta.request.annotation.RequestMapping;
import com.ethereal.meta.service.annotation.ServiceMapping;
import com.ethereal.meta.service.event.InterceptorEvent;
import com.ethereal.meta.service.event.delegate.InterceptorDelegate;
import com.ethereal.meta.utils.AnnotationUtils;
import lombok.Getter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;


public abstract class RawMeta {
    @Getter
    protected EventManager eventManager = new EventManager();
    @Getter
    protected AbstractTypeManager types = new AbstractTypeManager();
    @Getter
    protected InstanceManager instanceManager = new InstanceManager();


    @Getter
    private final ExceptionEvent exceptionEvent = new ExceptionEvent();
    @Getter
    private final LogEvent logEvent = new LogEvent();
    @Getter
    private final InterceptorEvent interceptorEvent = new InterceptorEvent();


    protected abstract void configure();
    protected abstract void type();
    protected abstract void initialize();
    protected abstract void uninitialize();


    public void onException(TrackException.ErrorCode code, String message) {
        onException(new TrackException(code,message,this));
    }

    public void onException(Exception exception)  {
        exceptionEvent.onEvent(exception);
    }

    public void onLog(TrackLog.LogCode code, String message){
        onLog(new TrackLog(code,message,this));
    }

    public void onLog(TrackLog log){
        log.setSender(this);
        logEvent.onEvent(log);
    }

    public boolean onInterceptor(RequestMeta requestMeta)
    {
        for (InterceptorDelegate item : interceptorEvent.getListeners())
        {
            if (!item.onInterceptor(requestMeta)) return false;
        }
        return true;
    }
    public void update(String msg){

    }
    public String save(){
        return null;
    }
}
