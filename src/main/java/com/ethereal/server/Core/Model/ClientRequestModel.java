package com.ethereal.server.Core.Model;

import com.google.gson.annotations.Expose;

import java.util.Arrays;
import java.util.HashMap;

public class ClientRequestModel {
    @Expose(serialize = false,deserialize = false)
    private ClientResponseModel result;
    @Expose
    private String type = "ER-1.0-ClientRequest";
    @Expose
    private String mapping;
    @Expose
    private HashMap<String ,String > params;
    @Expose
    private String id;

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

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ClientRequestModel{" +
                "result=" + result +
                ", type='" + type + '\'' +
                ", mapping='" + mapping + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
