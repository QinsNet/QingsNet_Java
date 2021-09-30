package com.ethereal.server.Core.Model;

import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Request.Abstract.Request;
import com.ethereal.server.Service.Abstract.Service;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Request.Abstract.Request;
import com.ethereal.server.Server.Abstract.BaseToken;
import com.ethereal.server.Server.Abstract.Server;
import com.ethereal.server.Service.Abstract.Service;

public class TrackLog {
    public enum LogCode { Core, Runtime }
    private String message;
    private LogCode code;
    private Server server;
    private Service service;
    private Request request;
    private Net net;
    private BaseToken token;

    public BaseToken getToken() {
        return token;
    }

    public void setToken(BaseToken token) {
        this.token = token;
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

    public Net getNet() {
        return net;
    }

    public void setNet(Net net) {
        this.net = net;
    }

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

    public TrackLog(LogCode code, String message) {
        this.message = message;
        this.code = code;
    }
}
