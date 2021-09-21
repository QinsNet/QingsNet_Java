package Old.RPCRequest;

import RPCNet.Event.ExceptionEvent;
import RPCNet.Event.LogEvent;
import RPCRequest.RequestConfig;

import java.lang.reflect.Proxy;

public class Request {
    private String name;
    private String netName;
    private ExceptionEvent exceptionEvent;
    private LogEvent logEvent;
    private RequestConfig config;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNetName() {
        return netName;
    }

    public void setNetName(String netName) {
        this.netName = netName;
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

    public RequestConfig getConfig() {
        return config;
    }

    public void setConfig(RequestConfig config) {
        this.config = config;
    }
    public static <T> T register(Class<T> interface_class, String netName, String serviceName, RequestConfig config){
        Request proxy = new Request();
        proxy.name = serviceName;
        proxy.netName = netName;
        proxy.config = config;
        return (T) Proxy.newProxyInstance(Request.class.getClassLoader(),new Class<?>[]{interface_class}, proxy);
    }
}
