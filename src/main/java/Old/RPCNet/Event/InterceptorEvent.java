package Old.RPCNet.Event;

import RPCNet.Event.Delegate.InterceptorDelegate;
import RPCNet.Event.Delegate.OnExceptionDelegate;
import RPCNet.Net;
import RPCservice.Service;
import sun.management.MethodInfo;

import java.util.Iterator;
import java.util.Vector;

public class InterceptorEvent {
    Vector<InterceptorDelegate> listeners= new Vector<>();

    public void register(InterceptorDelegate delegate){
        synchronized (listeners){
            if(!listeners.contains(delegate)) listeners.add(delegate);
        }
    }
    public void unRegister(OnExceptionDelegate delegate){
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


