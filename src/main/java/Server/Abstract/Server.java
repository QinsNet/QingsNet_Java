package Server.Abstract;

import Core.Event.ExceptionEvent;
import Core.Event.LogEvent;
import Server.Event.ListenerFailEvent;
import Server.Event.ListenerSuccessEvent;

import java.util.List;

public abstract class Server {
    public ExceptionEvent exceptionEvent = new ExceptionEvent();
    public LogEvent logEvent = new LogEvent();
    public ListenerFailEvent listenerFailEvent;
    public ListenerSuccessEvent listenerSuccessEvent;
    protected String netName;
    private ServerConfig config;
    protected HttpListener listener;
    protected List<String> prefixes;

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

    public String getNetName() {
        return netName;
    }

    public void setNetName(String netName) {
        this.netName = netName;
    }

    public ServerConfig getConfig() {
        return config;
    }

    public void setConfig(ServerConfig config) {
        this.config = config;
    }

    public HttpListener getListener() {
        return listener;
    }

    public void setListener(HttpListener listener) {
        this.listener = listener;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List<String> prefixes) {
        this.prefixes = prefixes;
    }
    public abstract void Start();
    public abstract void Close();
}
