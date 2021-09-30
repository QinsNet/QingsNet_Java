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
import com.ethereal.server.Server.Interface.IBaseToken;

import java.util.HashMap;

public abstract class BaseToken implements IBaseToken {
    protected ExceptionEvent exceptionEvent = new ExceptionEvent();
    protected LogEvent logEvent = new LogEvent();
    protected ConnectEvent connectEvent = new ConnectEvent();
    protected DisConnectEvent disConnectEvent = new DisConnectEvent();
    protected String netName;
    protected ServerConfig config;
    protected boolean canRequest = true;

    public abstract Object getKey();

    public abstract void setKey(Object key);

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

    public String getNetName() {
        return netName;
    }

    public void setNetName(String netName) {
        this.netName = netName;
    }

    public ServerConfig getConfig() {
        return config;
    }

    public void setConfig(ServerConfig config) {
        this.config = config;
    }

    public boolean getCanRequest() {
        return canRequest;
    }

    public void setCanRequest(boolean canRequest) {
        this.canRequest = canRequest;
    }

    public boolean Register()
    {
        Net net = NetCore.get(netName);
        net.getTokens().put(getKey(), this);
        return true;
    }

    public boolean Register(Boolean replace)
    {
        Net net = NetCore.get(netName);
        if (replace)
        {
            net.getTokens().remove(getKey());
        }
        net.getTokens().put(getKey(), this);
        return true;
    }
    /// <summary>
    /// 从Tokens表中注销Token信息
    /// </summary>
    /// <returns></returns>
    public Boolean UnRegister()
    {
        if (getKey() == null) return true;
        Net net = NetCore.get(netName);
        net.getTokens().remove(getKey());
        return true;
    }
    public HashMap<Object, BaseToken> GetTokens()
    {
        Net net = NetCore.get(netName);
        return net.getTokens();
    }

    public <T> T GetToken(Object key)
    {
        Net net = NetCore.get(netName);
        BaseToken token = net.getTokens().get(key);
        return (T)token;
    }

    public HashMap<Object, BaseToken> GetTokens(String netName)
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
