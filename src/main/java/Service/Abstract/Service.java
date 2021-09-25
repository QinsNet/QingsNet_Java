package Service.Abstract;

import Core.Event.ExceptionEvent;
import Core.Event.LogEvent;
import Core.Model.TrackException;
import Core.Model.TrackLog;
import Service.Interface.IService;
import sun.management.MethodInfo;

import java.lang.reflect.Method;
import java.util.HashMap;

public abstract class Service implements IService {
    private ExceptionEvent exceptionEvent;
    private LogEvent logEvent;
    protected HashMap<String, Method> methods = new HashMap<>();
    protected ServiceConfig config;
    protected Object instance;
    protected String netName;
    protected String name;

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

    public HashMap<String, Method> getMethods() {
        return methods;
    }

    public void setMethods(HashMap<String, Method> methods) {
        this.methods = methods;
    }

    public ServiceConfig getConfig() {
        return config;
    }

    public void setConfig(ServiceConfig config) {
        this.config = config;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public String getNetName() {
        return netName;
    }

    public void setNetName(String netName) {
        this.netName = netName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public abstract void Register(String netName, String service_name, Object instance, ServiceConfig config) throws TrackException;
    public void OnException(TrackException.ErrorCode code,String message){
        OnException(new TrackException(code,message));
    }
    public void OnException(TrackException e){
        if (exceptionEvent != null){
            e.setService(this);
            exceptionEvent.onEvent(e);
        }

    }
    public void OnLog(TrackLog.LogCode code,String message){
        OnLog(new TrackLog(code,message));
    }
    public void OnLog(TrackLog log){
        if(logEvent != null){
            log.setService(this);
            logEvent.onEvent(log);
        }
    }
}
