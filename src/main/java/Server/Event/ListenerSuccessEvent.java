package Server.Event;

import Server.Abstract.Server;
import Server.Event.Delegate.ListenerFailDelegate;
import Server.Event.Delegate.ListenerSuccessDelegate;

import java.util.Iterator;
import java.util.Vector;

public class ListenerSuccessEvent {
    Vector<ListenerSuccessDelegate> listeners= new Vector<>();
    public Vector<ListenerSuccessDelegate> getListeners() {
        return listeners;
    }

    public void setListeners(Vector<ListenerSuccessDelegate> listeners) {
        this.listeners = listeners;
    }


    public void register(ListenerSuccessDelegate delegate){
        synchronized (listeners){
            if(!listeners.contains(delegate)) listeners.add(delegate);
        }
    }
    public void unRegister(ListenerSuccessDelegate delegate){
        synchronized (listeners){
            Iterator<ListenerSuccessDelegate> iterator = listeners.iterator();
            while(iterator.hasNext() && iterator.next() == delegate){
                iterator.remove();
            }
        }
    }
    public void onEvent(Server server){
        synchronized (listeners){
            for (ListenerSuccessDelegate delegate:listeners) {
                delegate.ListenerSuccess(server);
            }
        }
    }
}
