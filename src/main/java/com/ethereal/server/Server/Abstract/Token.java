package com.ethereal.server.Server.Abstract;

import com.ethereal.server.Core.Event.ExceptionEvent;
import com.ethereal.server.Core.Event.LogEvent;
import com.ethereal.server.Core.Model.ClientResponseModel;
import com.ethereal.server.Core.Model.ServerRequestModel;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Core.Model.TrackLog;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Net.NetCore;
import com.ethereal.server.Server.Event.ConnectEvent;
import com.ethereal.server.Server.Event.DisConnectEvent;
import com.ethereal.server.Server.Interface.IToken;

import java.util.HashMap;

public abstract class Token implements IToken {
    protected ExceptionEvent exceptionEvent = new ExceptionEvent();
    protected LogEvent logEvent = new LogEvent();
    protected ConnectEvent connectEvent = new ConnectEvent();
    protected DisConnectEvent disConnectEvent = new DisConnectEvent();
    protected Server server;
    protected boolean canRequest = true;
    public Object key;

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

    public ConnectEvent getConnectEvent() {
        return connectEvent;
    }

    public void setConnectEvent(ConnectEvent connectEvent) {
        this.connectEvent = connectEvent;
    }

    public DisConnectEvent getDisConnectEvent() {
        return disConnectEvent;
    }

    public void setDisConnectEvent(DisConnectEvent disConnectEvent) {
        this.disConnectEvent = disConnectEvent;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public boolean getCanRequest() {
        return canRequest;
    }

    public void setCanRequest(boolean canRequest) {
        this.canRequest = canRequest;
    }

    public boolean Register()
    {
        server.getNet().getTokens().put(key, this);
        return true;
    }

    public boolean Register(Boolean replace)
    {
        if (replace)
        {
            server.getNet().getTokens().remove(key);
        }
        server.getNet().getTokens().put(key, this);
        return true;
    }
    /// <summary>
    /// 从Tokens表中注销Token信息
    /// </summary>
    /// <returns></returns>
    public Boolean UnRegister()
    {
        if (key == null) return true;
        server.getNet().getTokens().remove(key);
        return true;
    }
    public HashMap<Object, Token> GetTokens()
    {
        return server.getNet().getTokens();
    }

    public <T> T GetToken(Object key)
    {
        Token token = server.getNet().getTokens().get(key);
        return (T)token;
    }

    public HashMap<Object, Token> GetTokens(String netName)
    {
        Net net = NetCore.get(netName);
        return net.getTokens();
    }
    @Override
    public void onException(TrackException.ErrorCode code, String message){
        onException(new TrackException(code, message));
    }
    @Override
    public void onException(TrackException exception)  {
        exception.setToken(this);
        exceptionEvent.onEvent(exception);
    }
    @Override
    public void onLog(TrackLog.LogCode code, String message) {
        onLog(new TrackLog(code, message));
    }

    @Override
    public void onLog(TrackLog log) {
        log.setToken(this);
        logEvent.onEvent(log);
    }

    public void onConnect() {
        connectEvent.onEvent(this);
    }

    public void onDisconnectEvent() {
        disConnectEvent.onEvent(this);
    }

    public abstract void sendClientResponse(ClientResponseModel response);
    public abstract void sendServerRequest(ServerRequestModel request);
}
