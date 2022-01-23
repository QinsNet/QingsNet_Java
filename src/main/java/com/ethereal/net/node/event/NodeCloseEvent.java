package com.ethereal.net.node.event;

import com.ethereal.net.node.core.Node;
import com.ethereal.net.node.event.Delegate.NodeCloseDelegate;

import java.util.Iterator;
import java.util.Vector;

public class NodeCloseEvent {
    Vector<NodeCloseDelegate> listeners= new Vector<>();
    public Vector<NodeCloseDelegate> getListeners() {
        return listeners;
    }

    public void setListeners(Vector<NodeCloseDelegate> listeners) {
        this.listeners = listeners;
    }


    public void register(NodeCloseDelegate delegate){
        synchronized (listeners){
            if(!listeners.contains(delegate)) listeners.add(delegate);
        }
    }
    public void unRegister(NodeCloseDelegate delegate){
        synchronized (listeners){
            Iterator<NodeCloseDelegate> iterator = listeners.iterator();
            while(iterator.hasNext() && iterator.next() == delegate){
                iterator.remove();
            }
        }
    }
    public void onEvent(Node node){
        synchronized (listeners){
            for (NodeCloseDelegate delegate:listeners) {
                delegate.onDisConnect(node);
            }
        }
    }
}
