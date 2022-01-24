package com.ethereal.meta.meta.event;

import com.ethereal.meta.meta.event.delegate.ExceptionEventDelegate;

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
