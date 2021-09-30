package com.ethereal.server.Core.Model;

import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Request.Abstract.Request;
import com.ethereal.server.Service.Abstract.Service;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Request.Abstract.Request;
import com.ethereal.server.Server.Abstract.BaseToken;
import com.ethereal.server.Server.Abstract.Server;
import com.ethereal.server.Service.Abstract.Service;

public class TrackException extends Exception {
    public enum ErrorCode {Core, Runtime, NotEthereal}
    private Exception exception;
    private ErrorCode errorCode;
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

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
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

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public TrackException(ErrorCode errorCode, String message)
    {
        super(message);
        this.exception = this;
        this.errorCode = errorCode;
    }
    public TrackException(Exception e)
    {
        super("外部库错误");
        this.exception = e;
        this.errorCode = ErrorCode.NotEthereal;
    }
}
