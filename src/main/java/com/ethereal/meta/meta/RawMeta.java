package com.ethereal.meta.meta;

import com.ethereal.meta.core.aop.EventManager;
import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.meta.event.ExceptionEvent;
import com.ethereal.meta.meta.event.LogEvent;
import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.core.entity.TrackLog;
import com.ethereal.meta.core.instance.InstanceManager;
import com.ethereal.meta.core.type.AbstractTypeManager;
import com.ethereal.meta.net.network.INetwork;
import com.ethereal.meta.service.event.InterceptorEvent;
import com.ethereal.meta.service.event.delegate.InterceptorDelegate;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.HashMap;


public abstract class RawMeta {
    @Getter
    protected EventManager eventManager = new EventManager();
    @Getter
    protected AbstractTypeManager types = new AbstractTypeManager();
    @Getter
    protected InstanceManager instanceManager = new InstanceManager();
    @Getter
    protected HashMap<String,Meta> metas = new HashMap<>();
    @Getter
    protected String prefixes;
    @Getter
    private final ExceptionEvent exceptionEvent = new ExceptionEvent();
    @Getter
    private final LogEvent logEvent = new LogEvent();
    @Getter
    private final InterceptorEvent interceptorEvent = new InterceptorEvent();



    protected abstract void onConfigure();
    protected abstract void onRegister();
    protected abstract void onInstance();
    protected abstract void onInitialize();
    public abstract void onNetwork(INetwork parent);
    protected abstract void onUninitialize();

    public void onException(TrackException.ExceptionCode code, String message) {
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
