package com.ethereal.net.node.event;

import com.ethereal.net.node.core.Node;
import com.ethereal.net.node.event.Delegate.NodeStartDelegate;

import java.util.Iterator;
import java.util.Vector;

public class NodeStartEvent {
    Vector<NodeStartDelegate> listeners= new Vector<>();
    public Vector<NodeStartDelegate> getListeners() {
        return listeners;
    }

    public void setListeners(Vector<NodeStartDelegate> listeners) {
        this.listeners = listeners;
    }


    public void register(NodeStartDelegate delegate){
        synchronized (listeners){
            if(!listeners.contains(delegate)) listeners.add(delegate);
        }
    }
    public void unRegister(NodeStartDelegate delegate){
        synchronized (listeners){
            Iterator<NodeStartDelegate> iterator = listeners.iterator();
            while(iterator.hasNext() && iterator.next() == delegate){
                iterator.remove();
            }
        }
    }
    public void onEvent(Node node){
        synchronized (listeners){
            for (NodeStartDelegate delegate:listeners) {
                delegate.onConnect(node);
            }
        }
    }
}


