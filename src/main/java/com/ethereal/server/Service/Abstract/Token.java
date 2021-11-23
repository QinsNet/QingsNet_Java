package com.ethereal.server.Service.Abstract;

import com.ethereal.server.Core.BaseCore.BaseCore;
import com.ethereal.server.Core.EventRegister.ExceptionEvent;
import com.ethereal.server.Core.EventRegister.LogEvent;
import com.ethereal.server.Core.Model.ClientResponseModel;
import com.ethereal.server.Core.Model.ServerRequestModel;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Core.Model.TrackLog;
import com.ethereal.server.Server.EventRegister.ConnectEvent;
import com.ethereal.server.Server.EventRegister.DisConnectEvent;
import com.ethereal.server.Service.Interface.IToken;

import java.util.HashMap;

public abstract class Token extends BaseCore implements IToken {
    protected ConnectEvent connectEvent = new ConnectEvent();
    protected DisConnectEvent disConnectEvent = new DisConnectEvent();
    protected Service service;
    protected boolean canRequest = true;
    public Object key;

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

    public boolean getCanRequest() {
        return canRequest;
    }

    public void setCanRequest(boolean canRequest) {
        this.canRequest = canRequest;
    }

    public boolean Register()
    {
        service.getTokens().put(key, this);
        return true;
    }

    public boolean Register(Boolean replace)
    {
        if (replace)
        {
            service.getTokens().remove(key);
        }
        service.getTokens().put(key, this);
        return true;
    }
    /// <summary>
    /// 从Tokens表中注销Token信息
    /// </summary>
    /// <returns></returns>
    public Boolean UnRegister()
    {
        if (key == null) return true;
        service.getTokens().remove(key);
        return true;
    }
    public HashMap<Object, Token> GetTokens()
    {
        return service.getTokens();
    }

    public <T> T GetToken(Object key)
    {
        Token token = service.getTokens().get(key);
        return (T)token;
    }

    public void onConnect() {
        connectEvent.onEvent(this);
    }

    public void onDisconnect() {
        disConnectEvent.onEvent(this);
    }

    public abstract void sendClientResponse(ClientResponseModel response);
    public abstract void sendServerRequest(ServerRequestModel request);

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
