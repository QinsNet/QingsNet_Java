package com.ethereal.net.request.event;

import com.ethereal.net.core.entity.RequestMeta;
import com.ethereal.net.request.core.Request;
import com.ethereal.net.request.event.delegate.InterceptorDelegate;
import com.ethereal.net.service.core.Service;
import lombok.Getter;

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
    public void onEvent(Request request, RequestMeta requestMeta){
        synchronized (listeners){
            for (InterceptorDelegate delegate:listeners) {
                delegate.onInterceptor(request,requestMeta);
            }
        }
    }
}
