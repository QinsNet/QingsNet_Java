package com.ethereal.server.Core.Model;

import com.google.gson.annotations.Expose;

import java.util.Arrays;

public class ServerRequestModel {
    @Expose
    private String type = "ER-1.0-ServerRequest";
    @Expose
    private String methodId;
    @Expose
    private String[] params;
    @Expose
    private String service;


    public ServerRequestModel(String jsonRpc, String methodId, String[] params, String service) {
        this.type = jsonRpc;
        this.methodId = methodId;
        this.params = params;
        this.service = service;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMethodId() {
        return methodId;
    }

    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return "ServerRequestModel{" +
                "type='" + type + '\'' +
                ", methodId='" + methodId + '\'' +
                ", params=" + Arrays.toString(params) +
                ", service='" + service + '\'' +
                '}';
    }
}
