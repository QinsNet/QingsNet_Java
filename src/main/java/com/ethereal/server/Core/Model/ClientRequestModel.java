package com.ethereal.server.Core.Model;

import com.google.gson.annotations.Expose;

import java.util.Arrays;

public class ClientRequestModel {
    @Expose(serialize = false,deserialize = false)
    private ClientResponseModel result;
    @Expose
    private String type = "ER-1.0-ClientRequest";
    @Expose
    private String methodId;
    @Expose
    private String[] params;
    @Expose
    private String id;
    @Expose
    private String service;

    public ClientRequestModel(String jsonRpc, String service, String methodId, String[] params) {
        type = jsonRpc;
        methodId = methodId;
        params = params;
        service = service;
    }

    public ClientResponseModel getResult() {
        return result;
    }

    public void setResult(ClientResponseModel result) {
        this.result = result;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return "ClientRequestModel{" +
                "result=" + result +
                ", type='" + type + '\'' +
                ", methodId='" + methodId + '\'' +
                ", params=" + Arrays.toString(params) +
                ", id='" + id + '\'' +
                ", service='" + service + '\'' +
                '}';
    }
}
