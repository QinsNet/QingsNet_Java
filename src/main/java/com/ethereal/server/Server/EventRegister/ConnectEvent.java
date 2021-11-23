package com.ethereal.server.Server.EventRegister;

import com.ethereal.server.Service.Abstract.Token;
import com.ethereal.server.Server.EventRegister.Delegate.ConnectDelegate;

import java.util.Iterator;
import java.util.Vector;

public class ConnectEvent {
    Vector<ConnectDelegate> listeners= new Vector<>();
    public Vector<ConnectDelegate> getListeners() {
        return listeners;
    }

    public void setListeners(Vector<ConnectDelegate> listeners) {
        this.listeners = listeners;
    }


    public void register(ConnectDelegate delegate){
        synchronized (listeners){
            if(!listeners.contains(delegate)) listeners.add(delegate);
        }
    }
    public void unRegister(ConnectDelegate delegate){
        synchronized (listeners){
            Iterator<ConnectDelegate> iterator = listeners.iterator();
            while(iterator.hasNext() && iterator.next() == delegate){
                iterator.remove();
            }
        }
    }
    public void onEvent(Token token){
        synchronized (listeners){
            for (ConnectDelegate delegate:listeners) {
                delegate.onConnect(token);
            }
        }
    }
}


