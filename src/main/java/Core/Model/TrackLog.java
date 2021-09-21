package Core.Model;

import Net.Abstract.Net;
import Server.Abstract.Server;
import Service.Abstract.Service;

public class TrackLog {
    public enum LogCode{Core,Runtime}
    private String message;
    private LogCode code;

    private Net.Abstract.Net net;
    private Server server;
    private Service service;
    private Request request;
    private Token token;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LogCode getCode() {
        return code;
    }

    public void setCode(LogCode code) {
        this.code = code;
    }

    public Net getNet() {
        return net;
    }

    public void setNet(Net net) {
        this.net = net;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public TrackLog(LogCode code, String message) {
        this.message = message;
        this.code = code;
    }
}
