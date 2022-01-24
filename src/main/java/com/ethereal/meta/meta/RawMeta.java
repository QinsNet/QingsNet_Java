package com.ethereal.meta.meta;

import com.ethereal.meta.core.aop.EventManager;
import com.ethereal.meta.meta.event.ExceptionEvent;
import com.ethereal.meta.meta.event.LogEvent;
import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.core.entity.TrackLog;
import com.ethereal.meta.core.instance.InstanceManager;
import com.ethereal.meta.core.type.AbstractTypeManager;
import com.ethereal.meta.service.event.InterceptorEvent;
import com.ethereal.meta.service.event.delegate.InterceptorDelegate;
import lombok.Getter;

public class RawMeta {
    protected AbstractTypeManager types = new AbstractTypeManager();
    protected InstanceManager instanceManager = new InstanceManager();
    @Getter
    protected String prefixes;
    @Getter
    private final ExceptionEvent exceptionEvent = new ExceptionEvent();
    @Getter
    private final LogEvent logEvent = new LogEvent();
    @Getter
    private InterceptorEvent interceptorEvent = new InterceptorEvent();
    protected EventManager eventManager = new EventManager();
    public RawMeta(){

    }
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
        if (interceptorEvent != null)
        {
            for (InterceptorDelegate item : interceptorEvent.getListeners())
            {
                if (!item.onInterceptor(requestMeta)) return false;
            }
            return true;
        }
        else return true;
    }
    public void update(String meta){

    }
}
