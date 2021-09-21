package Server.Event;

import Net.Event.Delegate.InterceptorDelegate;
import Server.Abstract.Server;
import Server.Event.Delegate.ListenerFailDelegate;
import sun.management.MethodInfo;

import java.util.Iterator;
import java.util.Vector;

public class ListenerFailEvent {
    Vector<ListenerFailDelegate> listeners= new Vector<>();
    public Vector<ListenerFailDelegate> getListeners() {
        return listeners;
    }

    public void setListeners(Vector<ListenerFailDelegate> listeners) {
        this.listeners = listeners;
    }


    public void register(ListenerFailDelegate delegate){
        synchronized (listeners){
            if(!listeners.contains(delegate)) listeners.add(delegate);
        }
    }
    public void unRegister(ListenerFailDelegate delegate){
        synchronized (listeners){
            Iterator<ListenerFailDelegate> iterator = listeners.iterator();
            while(iterator.hasNext() && iterator.next() == delegate){
                iterator.remove();
            }
        }
    }
    public void onEvent(Server server){
        synchronized (listeners){
            for (ListenerFailDelegate delegate:listeners) {
                delegate.ListenerFail(server);
            }
        }
    }
}
