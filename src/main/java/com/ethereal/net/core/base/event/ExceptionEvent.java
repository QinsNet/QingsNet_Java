package com.ethereal.net.core.base.event;

import com.ethereal.net.core.base.BaseCore;
import com.ethereal.net.core.base.event.delegate.ExceptionEventDelegate;
import com.ethereal.net.core.entity.TrackException;

import java.util.Iterator;
import java.util.Vector;

public class ExceptionEvent {
    final Vector<ExceptionEventDelegate> listeners= new Vector<>();

    public void register(ExceptionEventDelegate delegate){
        synchronized (listeners){
            if(!listeners.contains(delegate)) listeners.add(delegate);
        }
    }
    public void unRegister(ExceptionEventDelegate delegate){
        synchronized (listeners){
            Iterator<ExceptionEventDelegate> iterator = listeners.iterator();
            while(iterator.hasNext() && iterator.next() == delegate){
                iterator.remove();
            }
        }
    }
    public void clear(){
        synchronized (listeners){
            listeners.clear();
        }
    }
    public void onEvent(Exception exception)  {
        synchronized (listeners){
            for (ExceptionEventDelegate delegate:listeners) {
                delegate.onException(exception);
            }
        }
    }
}
