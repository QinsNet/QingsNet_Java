package Old.NativeServer.Event;

import Model.RPCLog;
import NativeServer.Event.Delegate.OnLogDelegate;
import NativeServer.ServerListener;

import java.util.Iterator;
import java.util.Vector;

public class LogEvent {
    Vector<OnLogDelegate> listeners= new Vector<>();

    public void register(OnLogDelegate delegate){
        synchronized (listeners){
            if(!listeners.contains(delegate)) listeners.add(delegate);
        }
    }
    public void unRegister(OnLogDelegate delegate){
        synchronized (listeners){
            Iterator<OnLogDelegate> iterator = listeners.iterator();
            while(iterator.hasNext() && iterator.next() == delegate){
                iterator.remove();
            }
        }
    }
    public void onEvent(RPCLog log, ServerListener server){
        synchronized (listeners){
            for (OnLogDelegate delegate:listeners) {
                delegate.OnLog(log,server);
            }
        }
    }
}
