package com.ethereal.net.net.core;

import com.ethereal.net.core.base.BaseCore;
import com.ethereal.net.core.base.event.ExceptionEvent;
import com.ethereal.net.core.base.event.LogEvent;
import com.ethereal.net.core.entity.RequestMeta;
import com.ethereal.net.core.entity.TrackException;
import com.ethereal.net.core.entity.TrackLog;
import com.ethereal.net.node.core.Node;
import com.ethereal.net.request.core.Request;
import com.ethereal.net.service.core.Service;
import com.ethereal.net.service.event.delegate.InterceptorDelegate;
import com.ethereal.net.service.event.InterceptorEvent;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.HashMap;


public class Net implements INet {
    @Getter
    static NetConfig config;
    @Getter
    static String name;
    @Getter
    static HashMap<String, Service> services = new HashMap<>();
    @Getter
    static HashMap<String, Request> request = new HashMap<>();
    @Getter
    static InterceptorEvent interceptorEvent = new InterceptorEvent();
    @Getter
    static Node node;
    @Getter
    static final ExceptionEvent exceptionEvent = new ExceptionEvent();
    @Getter
    static final LogEvent logEvent = new LogEvent();

    public static void onException(TrackException.ErrorCode code, String message) {
        onException(new TrackException(code,message,Net.class));
    }

    public static void onException(Exception exception)  {
        exceptionEvent.onEvent(exception);
    }

    public static void onLog(TrackLog.LogCode code, String message) {
        onLog(new TrackLog(code,message,Net.class));
    }

    public static void onLog(TrackLog log) {
        logEvent.onEvent(log);
    }

    public static boolean OnInterceptor(Service service, Method method, Node node)
    {
        if (interceptorEvent != null)
        {
            for (InterceptorDelegate item : interceptorEvent.getListeners())
            {
                if (!item.onInterceptor(service, method, node)) return false;
            }
        }
        return true;
    }

    public static void receiveProcess(RequestMeta requestMeta){

    }
}
