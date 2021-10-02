package com.ethereal.server.Core.Model;

import com.google.gson.annotations.Expose;

public class ClientResponseModel {
    @Expose
    private String type = "ER-1.0-ClientResponse";
    @Expose
    private String result = null;
    @Expose
    private String resultType = null;
    @Expose
    private Error error = null;
    @Expose
    private String id = null;
    @Expose
    private String service = null;

    public ClientResponseModel(String result, String resultType, Error error, String id, String service) {
        this.result = result;
        this.resultType = resultType;
        this.error = error;
        this.id = id;
        this.service = service;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
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
        return "ClientResponseModel{" +
                "type='" + type + '\'' +
                ", result='" + result + '\'' +
                ", resultType='" + resultType + '\'' +
                ", error=" + error +
                ", id='" + id + '\'' +
                ", service='" + service + '\'' +
                '}';
    }
}
