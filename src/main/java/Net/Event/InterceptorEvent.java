package Net.Event;

import Net.Event.Delegate.InterceptorDelegate;
import sun.management.MethodInfo;

import java.util.Iterator;
import java.util.Vector;

public class InterceptorEvent {
    Vector<InterceptorDelegate> listeners= new Vector<>();
    public Vector<InterceptorDelegate> getListeners() {
        return listeners;
    }

    public void setListeners(Vector<InterceptorDelegate> listeners) {
        this.listeners = listeners;
    }


    public void register(InterceptorDelegate delegate){
        synchronized (listeners){
            if(!listeners.contains(delegate)) listeners.add(delegate);
        }
    }
    public void unRegister(InterceptorDelegate delegate){
        synchronized (listeners){
            Iterator<InterceptorDelegate> iterator = listeners.iterator();
            while(iterator.hasNext() && iterator.next() == delegate){
                iterator.remove();
            }
        }
    }
    public void onEvent(Service service, MethodInfo method){
        synchronized (listeners){
            for (InterceptorDelegate delegate:listeners) {
                delegate.Interceptor(service,method);
            }
        }
    }
}
