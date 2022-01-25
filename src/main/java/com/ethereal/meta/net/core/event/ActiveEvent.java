package com.ethereal.meta.net.core.event;

import com.ethereal.meta.net.core.Net;
import com.ethereal.meta.net.core.event.Delegate.ActiveDelegate;
import com.ethereal.meta.net.network.Network;

import java.util.Iterator;
import java.util.Vector;

public class ActiveEvent {
    Vector<ActiveDelegate> listeners= new Vector<>();
    public Vector<ActiveDelegate> getListeners() {
        return listeners;
    }

    public void setListeners(Vector<ActiveDelegate> listeners) {
        this.listeners = listeners;
    }


    public void register(ActiveDelegate delegate){
        synchronized (listeners){
            if(!listeners.contains(delegate)) listeners.add(delegate);
        }
    }
    public void unRegister(ActiveDelegate delegate){
        synchronized (listeners){
            Iterator<ActiveDelegate> iterator = listeners.iterator();
            while(iterator.hasNext() && iterator.next() == delegate){
                iterator.remove();
            }
        }
    }
    public void onEvent(Net net){
        synchronized (listeners){
            for (ActiveDelegate delegate:listeners) {
                delegate.onActive(net);
            }
        }
    }
}


