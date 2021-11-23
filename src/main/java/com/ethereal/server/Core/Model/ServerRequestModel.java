package com.ethereal.server.Core.Model;

import com.google.gson.annotations.Expose;

import java.util.Arrays;
import java.util.HashMap;

public class ServerRequestModel {
    @Expose
    private String type = "ER-1.0-ServerRequest";
    @Expose
    private String mapping;
    @Expose
    private HashMap<String ,String > params;
    @Expose
    private String service;

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

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "ServerRequestModel{" +
                "type='" + type + '\'' +
                ", mapping='" + mapping + '\'' +
                ", service='" + service + '\'' +
                '}';
    }
}
