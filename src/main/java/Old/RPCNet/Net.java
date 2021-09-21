package Old.RPCNet;

import Model.ClientRequestModel;
import Model.RPCException;
import Model.RPCLog;
import Model.RPCType;
import NativeServer.ServerListener;
import RPCNet.Event.ExceptionEvent;
import RPCNet.Event.LogEvent;
import RPCNet.Interface.IClientRequestReceive;
import RPCNet.Interface.IClientResponseSend;
import RPCNet.Interface.IServerRequestSend;
import RPCNet.NetConfig;
import RPCservice.Service;

import java.lang.reflect.Method;
import java.util.HashMap;

public class Net {
    private ServerListener server;
    private NetConfig config;
    private IClientResponseSend iClientResponseSend;
    private IClientRequestReceive iClientRequestReceive;
    private IServerRequestSend iServerRequestSend;
    private String name;
    private HashMap<String, Service> services = new HashMap<>();
    private HashMap<String, Object> requests = new HashMap<>();
    private ExceptionEvent exceptionEvent = new ExceptionEvent();
    private LogEvent logEvent = new LogEvent();

    public ServerListener getServer() {
        return server;
    }

    public void setServer(ServerListener server) {
        this.server = server;
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

    public void onLog(RPCLog.LogCode code, String message) {
        onLog(new RPCLog(code, message));
    }

    public void onLog(RPCLog log) {
        logEvent.onEvent(log, this);
    }

    public void OnRequestException(Exception exception, Request request) throws Exception {
        onException(exception);
    }

    public void OnRequestLog(RPCLog log, Request request) {
        onLog(log);
    }

    public void OnServiceException(Exception exception, Service service) throws Exception {
        onException(exception);
    }

    public void OnServiceLog(RPCLog log, Service service) {
        onLog(log);
    }

    public NetConfig getConfig() {
        return config;
    }

    public void setConfig(NetConfig config) {
        this.config = config;
    }

    public IClientResponseSend getiClientResponseSend() {
        return iClientResponseSend;
    }

    public void setiClientResponseSend(IClientResponseSend iClientResponseSend) {
        this.iClientResponseSend = iClientResponseSend;
    }

    public IClientRequestReceive getiClientRequestReceive() {
        return iClientRequestReceive;
    }

    public void setiClientRequestReceive(IClientRequestReceive iClientRequestReceive) {
        this.iClientRequestReceive = iClientRequestReceive;
    }

    public IServerRequestSend getiServerRequestSend() {
        return iServerRequestSend;
    }

    public void setiServerRequestSend(IServerRequestSend iServerRequestSend) {
        this.iServerRequestSend = iServerRequestSend;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Service> getServices() {
        return services;
    }

    public void setServices(HashMap<String, Service> services) {
        this.services = services;
    }

    public HashMap<String, Object> getRequests() {
        return requests;
    }

    public void setRequests(HashMap<String, Object> requests) {
        this.requests = requests;
    }

    public Net() {

    }


    private void ClientRequestProcess(ClientRequestModel request) {
        Method method;
        Service service = services.get(request.getService());
        if (service != null) {
            method = service.getMethods().get(request.getMethodId());
            if (method != null) {
                //开始序列化参数
                String[] param_id = request.getMethodId().split("-");
                for (int i = 1, j = 0; i < param_id.length; i++, j++) {
                    RPCType rpcType = service.getTypes().getTypesByName().get(param_id[i]);
                    if (rpcType == null) {
                        service.onException(new RPCException(RPCException.ErrorCode.Runtime, String.format("RPC中的%s类型参数尚未被注册！", param_id[i])));
                    } else
                        request.getParams()[j] = rpcType.getDeserialize().Deserialize((String) request.getParams()[j]);
                }
                method.invoke(service, request.getParams());
            } else {
                service.onException(new RPCException(RPCException.ErrorCode.Runtime, String.format("%s-%s-%s Not Found", name, request.getService(), request.getMethodId())));
            }
        } else {
            onException(new RPCException(RPCException.ErrorCode.Runtime, String.format("%s-%s Not Found", name, request.getService())));
        }

    }
}
