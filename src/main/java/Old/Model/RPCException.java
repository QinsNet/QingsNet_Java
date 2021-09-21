package Old.Model;

import RPCNet.Net;
import RPCRequest.Request;
import RPCservice.Service;

public class RPCException extends Exception {
    public enum ErrorCode {Core, Runtime}
    private ErrorCode errorCode;
    private String message;
    private Net net;
    private Service service;
    private Request request;
    private Exception exception;

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Net getNet() {
        return net;
    }

    public void setNet(Net net) {
        this.net = net;
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

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public RPCException(ErrorCode errorCode,String message)
    {
        super(message);
        this.message = message;
        this.errorCode = errorCode;
    }

    public RPCException(String message, String message1) {
        super(message);
        this.message = message1;
    }

    public RPCException(String message, String message1, Exception exception) {
        super(message);
        this.message = message1;
        this.exception = exception;
    }
}
