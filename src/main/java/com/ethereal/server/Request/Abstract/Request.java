package com.ethereal.server.Request.Abstract;

import com.ethereal.server.Core.Event.ExceptionEvent;
import com.ethereal.server.Core.Event.LogEvent;
import com.ethereal.server.Core.Model.AbstractTypes;
import com.ethereal.server.Core.Model.ClientRequestModel;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Core.Model.TrackLog;
import com.ethereal.server.Request.Interface.IRequest;
import com.ethereal.server.Server.Abstract.Server;
import net.sf.cglib.proxy.*;

import java.util.concurrent.ConcurrentHashMap;

public abstract class Request implements IRequest {
    protected final ConcurrentHashMap<Integer,ClientRequestModel> tasks = new ConcurrentHashMap<>();
    protected String name;
    protected String netName;
    protected RequestConfig config;
    protected Server server;//连接体
    protected ExceptionEvent exceptionEvent = new ExceptionEvent();
    protected LogEvent logEvent = new LogEvent();
    protected AbstractTypes types = new AbstractTypes();

    public AbstractTypes getTypes() {
        return types;
    }

    public void setTypes(AbstractTypes types) {
        this.types = types;
    }

    public static Request register(Class<Request> instance_class){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(instance_class);
        RequestMethodInterceptor interceptor = new RequestMethodInterceptor();
        Callback noOp= NoOp.INSTANCE;
        enhancer.setCallbacks(new Callback[]{noOp,interceptor});
        enhancer.setCallbackFilter(method -> {
            if(method.getAnnotation(com.ethereal.server.Request.Annotation.Request.class) != null){
                return 1;
            }
            else return 0;
        });
        Request instance = (Request)enhancer.create();
        interceptor.setInstance(instance);
        return instance;
    }
    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public RequestConfig getConfig() {
        return config;
    }
    public void setConfig(RequestConfig config) {
        this.config = config;
    }
    public ConcurrentHashMap<Integer, ClientRequestModel> getTasks() {
        return tasks;
    }

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
    @Override

    public void onException(TrackException.ErrorCode code, String message) {
        onException(new TrackException(code,message));
    }
    @Override
    public void onException(TrackException exception)  {
        exception.setRequest(this);
        exceptionEvent.onEvent(exception);
    }
    @Override

    public void onLog(TrackLog.LogCode code, String message){
        onLog(new TrackLog(code,message));
    }
    @Override
    public void onLog(TrackLog log){
        log.setRequest(this);
        logEvent.onEvent(log);
    }

}
