package com.ethereal.server.Server.Abstract;

import com.ethereal.server.Core.Event.ExceptionEvent;
import com.ethereal.server.Core.Event.LogEvent;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Core.Model.TrackLog;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Server.Delegate.CreateInstanceDelegate;
import com.ethereal.server.Server.Event.ListenerFailEvent;
import com.ethereal.server.Server.Event.ListenerSuccessEvent;
import com.ethereal.server.Server.Interface.IServer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class Server implements IServer {
    protected ExceptionEvent exceptionEvent = new ExceptionEvent();
    protected LogEvent logEvent = new LogEvent();
    protected ListenerFailEvent listenerFailEvent = new ListenerFailEvent();
    protected ListenerSuccessEvent listenerSuccessEvent = new ListenerSuccessEvent();
    protected Net net;
    protected ServerConfig config;
    protected List<String> prefixes;
    protected CreateInstanceDelegate createMethod;

    public Server(List<String > prefixes, CreateInstanceDelegate createMethod){
        this.prefixes = prefixes;
        this.createMethod = createMethod;
    }

    public CreateInstanceDelegate getCreateMethod() {
        return createMethod;
    }

    public void setCreateMethod(CreateInstanceDelegate createMethod) {
        this.createMethod = createMethod;
    }


    public ExceptionEvent getExceptionEvent() {
        return exceptionEvent;
    }

    public void setExceptionEvent(ExceptionEvent exceptionEvent) {
        this.exceptionEvent = exceptionEvent;
    }

    public LogEvent getLogEvent() {
        return logEvent;
    }

    public void setLogEvent(LogEvent logEvent) {
        this.logEvent = logEvent;
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
    @Override
    public void onException(TrackException.ErrorCode code, String message){
        onException(new TrackException(code, message));
    }
    @Override
    public void onException(TrackException exception)  {
        exception.setServer(this);
        exceptionEvent.onEvent(exception);
    }
    @Override
    public void onLog(TrackLog.LogCode code, String message) {
        onLog(new TrackLog(code, message));
    }
    @Override
    public void onLog(TrackLog log) {
        log.setServer(this);
        logEvent.onEvent(log);
    }


    public void onListenerSuccess() {
        listenerSuccessEvent.onEvent(this);
    }

    public void onListenerFailEvent() {
        listenerFailEvent.onEvent(this);
    }
}
