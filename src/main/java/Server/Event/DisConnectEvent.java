package Server.Event;

import Server.Abstract.Server;
import Server.Abstract.Token;
import Server.Event.Delegate.DisConnectDelegate;
import Server.Event.Delegate.ListenerFailDelegate;

import java.util.Iterator;
import java.util.Vector;

public class DisConnectEvent {
    Vector<DisConnectDelegate> listeners= new Vector<>();
    public Vector<DisConnectDelegate> getListeners() {
        return listeners;
    }

    public void setListeners(Vector<DisConnectDelegate> listeners) {
        this.listeners = listeners;
    }


    public void register(DisConnectDelegate delegate){
        synchronized (listeners){
            if(!listeners.contains(delegate)) listeners.add(delegate);
        }
    }
    public void unRegister(DisConnectDelegate delegate){
        synchronized (listeners){
            Iterator<DisConnectDelegate> iterator = listeners.iterator();
            while(iterator.hasNext() && iterator.next() == delegate){
                iterator.remove();
            }
        }
    }
    public void onEvent(Token token){
        synchronized (listeners){
            for (DisConnectDelegate delegate:listeners) {
                delegate.DisConnect(token);
            }
        }
    }
}
