package com.ethereal.meta.request.event;

import com.ethereal.meta.request.core.Request;
import com.ethereal.meta.request.event.delegate.InterceptorDelegate;
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
    public void onEvent(Request request, com.ethereal.meta.core.entity.RequestMeta requestMeta){
        synchronized (listeners){
            for (InterceptorDelegate delegate:listeners) {
                delegate.onInterceptor(request,requestMeta);
            }
        }
    }
}
