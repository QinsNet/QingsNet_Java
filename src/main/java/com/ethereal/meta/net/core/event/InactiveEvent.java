package com.ethereal.meta.net.core.event;

import com.ethereal.meta.net.core.Net;
import com.ethereal.meta.net.network.Network;
import com.ethereal.meta.net.core.event.Delegate.InactiveDelegate;

import java.util.Iterator;
import java.util.Vector;

public class InactiveEvent {
    Vector<InactiveDelegate> listeners= new Vector<>();
    public Vector<InactiveDelegate> getListeners() {
        return listeners;
    }

    public void setListeners(Vector<InactiveDelegate> listeners) {
        this.listeners = listeners;
    }


    public void register(InactiveDelegate delegate){
        synchronized (listeners){
            if(!listeners.contains(delegate)) listeners.add(delegate);
        }
    }
    public void unRegister(InactiveDelegate delegate){
        synchronized (listeners){
            Iterator<InactiveDelegate> iterator = listeners.iterator();
            while(iterator.hasNext() && iterator.next() == delegate){
                iterator.remove();
            }
        }
    }
    public void onEvent(Net net){
        synchronized (listeners){
            for (InactiveDelegate delegate:listeners) {
                delegate.onInactive(net);
            }
        }
    }
}
