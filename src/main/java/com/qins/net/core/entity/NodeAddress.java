package com.qins.net.core.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class NodeAddress {
    String host;
    int port;
    public NodeAddress(String address){
        String[] value = address.split(":");
        host = value[0];
        port = Integer.parseInt(value[1]);
    }
}
