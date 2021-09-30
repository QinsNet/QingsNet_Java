package com.ethereal.server.Net.NetNode.Model;

import com.google.gson.annotations.Expose;

public class RequestNode {
    @Expose
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
