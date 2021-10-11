package com.ethereal.server.Service.Event;

import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Server.Abstract.Token;
import com.ethereal.server.Service.Abstract.Service;
import com.ethereal.server.Service.Event.Delegate.InterceptorDelegate;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Vector;

public class InterceptorEvent {
    Vector<InterceptorDelegate> listeners= new Vector<>();
    public Vector<InterceptorDelegate> getListeners() {
        return listeners;
    }

    public void setListeners(Vector<InterceptorDelegate> listeners) {
        this.listeners = listeners;
    }


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
    public void onEvent(Net net, Service service, Method method, Token token){
        synchronized (listeners){
            for (InterceptorDelegate delegate:listeners) {
                delegate.onInterceptor(net,service,method,token);
            }
        }
    }
}
