package com.ethereal.server.Core.Model;

import com.google.gson.annotations.Expose;

import java.util.Arrays;

public class ServerRequestModel {
    @Expose
    private String Type = "ER-1.0-ServerRequest";
    @Expose
    private String MethodId;
    @Expose
    private Object[] Params;
    @Expose
    private String Service;


    public ServerRequestModel(String jsonRpc, String methodId, Object[] params, String service) {
        this.Type = jsonRpc;
        this.MethodId = methodId;
        this.Params = params;
        this.Service = service;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        this.Type = type;
    }

    public String getMethodId() {
        return MethodId;
    }

    public void setMethodId(String methodId) {
        this.MethodId = methodId;
    }

    public Object[] getParams() {
        return Params;
    }

    public void setParams(Object[] params) {
        this.Params = params;
    }

    public String getService() {
        return Service;
    }

    public void setService(String service) {
        this.Service = service;
    }

    @Override
    public String toString() {
        return "ServerRequestModel{" +
                "Type='" + Type + '\'' +
                ", MethodId='" + MethodId + '\'' +
                ", Params=" + Arrays.toString(Params) +
                ", com.ethereal.server.Service='" + Service + '\'' +
                '}';
    }
}
