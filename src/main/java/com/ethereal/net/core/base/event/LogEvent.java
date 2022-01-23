package com.ethereal.net.core.base.event;

import com.ethereal.net.core.base.BaseCore;
import com.ethereal.net.core.base.event.delegate.LogEventDelegate;
import com.ethereal.net.core.entity.TrackLog;

import java.util.Iterator;
import java.util.Vector;

public class LogEvent {
    final Vector<LogEventDelegate> listeners= new Vector<>();

    public void register(LogEventDelegate delegate){
        synchronized (listeners){
            if(!listeners.contains(delegate)) listeners.add(delegate);
        }
    }
    public void unRegister(LogEventDelegate delegate){
        synchronized (listeners){
            Iterator<LogEventDelegate> iterator = listeners.iterator();
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
    public void onEvent(TrackLog log){
        synchronized (listeners){
            for (LogEventDelegate delegate:listeners) {
                delegate.onLog(log);
            }
        }
    }
}
