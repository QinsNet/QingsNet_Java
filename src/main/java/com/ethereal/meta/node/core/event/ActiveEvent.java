package com.ethereal.meta.node.core.event;

import com.ethereal.meta.node.core.Node;
import com.ethereal.meta.node.core.event.Delegate.ActiveDelegate;

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
    public void onEvent(Node node){
        synchronized (listeners){
            for (ActiveDelegate delegate:listeners) {
                delegate.onActive(node);
            }
        }
    }
}


