package com.ethereal.server.Net.Abstract;

import com.ethereal.server.Core.BaseCore.BaseCore;
import com.ethereal.server.Core.Enums.NetType;
import com.ethereal.server.Net.Interface.INet;
import com.ethereal.server.Service.Abstract.Token;
import com.ethereal.server.Server.Abstract.Server;
import com.ethereal.server.Service.Abstract.Service;
import com.ethereal.server.Service.EventRegister.Delegate.InterceptorDelegate;
import com.ethereal.server.Service.EventRegister.InterceptorEvent;

import java.lang.reflect.Method;
import java.util.HashMap;

public abstract class Net extends BaseCore implements INet {
    protected NetConfig config;
    protected String name;
    protected NetType netType;
    protected Server server;
    protected HashMap<String, Service> services = new HashMap<>();
    protected InterceptorEvent interceptorEvent = new InterceptorEvent();
    public Net(String name){
        this.name = name;
    }
    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
    public NetType getNetType() {
        return netType;
    }

    public void setNetType(NetType netType) {
        this.netType = netType;
    }

    public String getName() {
        return name;
    }

    public NetConfig getConfig() {
        return config;
    }

    public void setConfig(NetConfig config) {
        this.config = config;
    }

    public HashMap<String, Service> getServices() {
        return services;
    }

    public void setServices(HashMap<String, Service> services) {
        this.services = services;
    }

    public boolean OnInterceptor(Service service, Method method, Token token)
    {
        if (interceptorEvent != null)
        {
            for (InterceptorDelegate item : interceptorEvent.getListeners())
            {
                if (!item.onInterceptor(this,service, method, token)) return false;
            }
            return true;
        }
        else return true;
    }


}
