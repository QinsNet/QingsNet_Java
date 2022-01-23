package com.ethereal.net.service.event;

import com.ethereal.net.core.entity.RequestMeta;
import com.ethereal.net.net.core.Net;
import com.ethereal.net.service.core.Service;
import com.ethereal.net.service.event.delegate.InterceptorDelegate;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Vector;

public class InterceptorEvent {
    @Getter
    final Vector<InterceptorDelegate> listeners= new Vector<>();

    public void register(InterceptorDelegate delegate){
        synchronized (listeners){
            if(!listeners.contains(delegate)) listeners.add(delegate);
        }
    }
    public void unRegister(InterceptorDelegate delegate){
        synchronized (listeners){
            Iterator<InterceptorDelegate> iterator = listeners.iterator();
            while(iterator.hasNext() && iterator.next() == delegate){
                iterator.remove();
            }
        }
    }
    public void onEvent(RequestMeta requestMeta){
        synchronized (listeners){
            for (InterceptorDelegate delegate:listeners) {
                delegate.onInterceptor(requestMeta);
            }
        }
    }
}
