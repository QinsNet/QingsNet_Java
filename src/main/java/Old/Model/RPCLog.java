package Old.Model;

import RPCNet.Net;
import RPCRequest.Request;
import RPCservice.Service;
import com.sun.corba.se.spi.activation.Server;

public class RPCLog {
    public enum LogCode{Core,Runtime}

    private String message;
    private LogCode code;
    private Net net;
    private Server server;
    private Service service;
    private Request request;


    public LogCode getCode() {
        return code;
    }

    public void setCode(LogCode code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RPCLog(LogCode code, String message) {
        this.message = message;
        this.code = code;
    }
}
