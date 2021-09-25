package Server.Abstract;

import Core.Event.ExceptionEvent;
import Core.Event.LogEvent;
import Core.Model.ClientResponseModel;
import Core.Model.ServerRequestModel;
import Core.Model.TrackException;
import Core.Model.TrackLog;
import Net.Abstract.Net;
import Net.NetCore;
import Server.Event.ConnectEvent;
import Server.Event.DisConnectEvent;
import Server.Interface.IBaseToken;

public abstract class Token implements IBaseToken {
    private LogEvent logEvent;
    private ExceptionEvent exceptionEvent;
    private ConnectEvent connectEvent;
    private DisConnectEvent disConnectEvent;

    protected String netName;
    protected ServerConfig config;
    protected boolean isWebSocket;
    private Object Key;

    public Object getKey() {
        return Key;
    }

    public void setKey(Object key) {
        Key = key;
    }

    public LogEvent getLogEvent() {
        return logEvent;
    }

    public void setLogEvent(LogEvent logEvent) {
        this.logEvent = logEvent;
    }

    public ExceptionEvent getExceptionEvent() {
        return exceptionEvent;
    }

    public void setExceptionEvent(ExceptionEvent exceptionEvent) {
        this.exceptionEvent = exceptionEvent;
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

    public boolean isWebSocket() {
        return isWebSocket;
    }

    public void setWebSocket(boolean webSocket) {
        isWebSocket = webSocket;
    }
    public void OnException(TrackException.ErrorCode code, String message)
    {
        OnException(new TrackException(code, message));
    }
    public void OnException(TrackException e)
    {
        if (exceptionEvent != null)
        {
            e.setToken(this);
            exceptionEvent.onEvent(e);
        }
    }

    public void OnLog(TrackLog.LogCode code, String message)
    {
        OnLog(new TrackLog(code, message));
    }
    public void OnLog(TrackLog log)
    {
        if (logEvent != null)
        {
           log.setToken(this);
            logEvent.onEvent(log);
        }
    }
    /// <summary>
    /// 连接时激活连接事件
    /// </summary>
    public void OnConnect()
    {
       // ConnectEvent?.Invoke(this);
        connectEvent.onEvent(this);
    }
    /// <summary>
    /// 断开连接时激活断开连接事件
    /// </summary>
    public void OnDisConnect()
    {
        //DisConnectEvent?.Invoke(this);
        disConnectEvent.onEvent(this);
    }
    public abstract void DisConnect(String reson);

    protected abstract void SendClientResponse(ClientResponseModel response);
    protected abstract void SendServerRequest(ServerRequestModel request);

    /// <summary>
    /// 注册Token信息至Tokens表
    /// </summary>
    /// <param name="replace">当已存在Token信息，是否替换</param>
    /// <returns></returns>
    public boolean Register(boolean replace)
    {
        Net net = new Net();
        if (!NetCore.Get(NetName, out Net.Abstract.Net net))
        {
            throw new TrackException(TrackException.ErrorCode.Runtime, "{NetName}Net未找到");
        }
        if (replace)
        {
            net.Tokens.TryRemove(Key, out Token token);
        }
        return net.Tokens.TryAdd(Key, this);
    }

}

