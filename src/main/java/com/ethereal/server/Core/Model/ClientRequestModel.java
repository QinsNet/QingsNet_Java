package com.ethereal.server.Core.Model;

import com.google.gson.annotations.Expose;

public class ClientRequestModel {
    @Expose(serialize = false,deserialize = false)
    private ClientResponseModel Result;
    @Expose
    private String Type = "ER-1.0-ClientRequest";
    @Expose
    private String MethodId;
    @Expose
    private Object[] Params;
    @Expose
    private String Id;
    @Expose
    private String Service;

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getMethodId() {
        return MethodId;
    }

    public void setMethodId(String methodId) {
        MethodId = methodId;
    }

    public Object[] getParams() {
        return Params;
    }

    public void setParams(Object[] params) {
        Params = params;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getService() {
        return Service;
    }

    public void setService(String service) {
        Service = service;
    }

    public ClientRequestModel(String jsonRpc, String service, String methodId, String[] params) {
        Type = jsonRpc;
        MethodId = methodId;
        Params = params;
        Service = service;
    }

    public void setResult(ClientResponseModel result) {
        this.Result = result;
    }

    public ClientResponseModel getResult()  {
        return this.Result;
    }

    @Override
    public String toString() {
        return "ClientRequestModel{" +
                "Type='" + Type + '\'' +
                ", MethodId='" + MethodId + '\'' +
                ", Params=" + com.ethereal.server.Utils.Utils.gson.toJson(Params) +
                ", com.ethereal.server.Service='" + Service + '\'' +
                '}';
    }
}
