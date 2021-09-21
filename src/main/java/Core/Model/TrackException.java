package Core.Model;

import Net.Abstract.Net;
import Server.Abstract.Server;
import Service.Abstract.Service;

public class TrackException extends java.lang.Exception{
    public enum ErrorCode{Core,Runtime,NotEthereal}
    private ErrorCode errorCode;
    private Net net;
    private Server server;
    private Service service;
    private Request request;
    private Token token;
    private Exception exception;


    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
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

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public TrackException(ErrorCode errorCode, String message) {
        super(message);
        this.exception = this;
        this.errorCode = errorCode;
    }

    public TrackException(java.lang.Exception e) {
        super("外部库错误");
        this.errorCode =ErrorCode.NotEthereal;
        this.exception = e;
    }
    public TrackException(String message){
        super(message);
        exception = this;
    }
}
