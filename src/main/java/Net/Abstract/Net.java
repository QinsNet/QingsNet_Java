package Net.Abstract;

import Core.Event.ExceptionEvent;
import Core.Event.LogEvent;
import Net.Interface.INet;
import Old.Model.ClientRequestModel;
import Old.Model.ClientResponseModel;
import Server.Abstract.Server;
import Service.Abstract.Service;

import java.util.HashMap;

public abstract class Net implements INet {

    public enum NetType{WebSocket}
    //service映射表
    protected HashMap<String, Service> services = new HashMap<>();
    //request映射表
    protected HashMap<String, Object> requests = new HashMap<>();
    protected NetConfig config;
    protected Server server;
    //net网关名
    protected String name;
    protected NetType type;
    protected ExceptionEvent exception = new ExceptionEvent();
    protected LogEvent logEvent = new LogEvent();

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

    public NetConfig getConfig() {
        return config;
    }

    public void setConfig(NetConfig config) {
        this.config = config;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NetType getType() {
        return type;
    }

    public void setType(NetType type) {
        this.type = type;
    }



    @Override
    public ClientResponseModel ClientRequestReceieveProcess(ClientRequestModel request) {
        return null;
    }
}
