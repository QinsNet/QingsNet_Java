package com.ethereal.server.Server.Abstract;

import com.ethereal.server.Core.BaseCore.BaseCore;
import com.ethereal.server.Core.EventRegister.ExceptionEvent;
import com.ethereal.server.Core.EventRegister.LogEvent;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Core.Model.TrackLog;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Server.Delegate.CreateInstanceDelegate;
import com.ethereal.server.Server.EventRegister.ListenerFailEvent;
import com.ethereal.server.Server.EventRegister.ListenerSuccessEvent;
import com.ethereal.server.Server.Interface.IServer;

import java.util.List;

public abstract class Server extends BaseCore implements IServer {
    protected ListenerFailEvent listenerFailEvent = new ListenerFailEvent();
    protected ListenerSuccessEvent listenerSuccessEvent = new ListenerSuccessEvent();
    protected Net net;
    protected ServerConfig config;
    protected List<String> prefixes;

    public Server(List<String > prefixes){
        this.prefixes = prefixes;
    }

    public ListenerFailEvent getListenerFailEvent() {
        return listenerFailEvent;
    }

    public void setListenerFailEvent(ListenerFailEvent listenerFailEvent) {
        this.listenerFailEvent = listenerFailEvent;
    }

    public ListenerSuccessEvent getListenerSuccessEvent() {
        return listenerSuccessEvent;
    }

    public void setListenerSuccessEvent(ListenerSuccessEvent listenerSuccessEvent) {
        this.listenerSuccessEvent = listenerSuccessEvent;
    }

    public Net getNet() {
        return net;
    }

    public void setNet(Net net) {
        this.net = net;
    }

    public ServerConfig getConfig() {
        return config;
    }

    public void setConfig(ServerConfig config) {
        this.config = config;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List<String> prefixes) {
        this.prefixes = prefixes;
    }

    public void onListenerSuccess() {
        listenerSuccessEvent.onEvent(this);
    }

    public void onListenerFailEvent() {
        listenerFailEvent.onEvent(this);
    }
}
