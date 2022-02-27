package com.qins.net.core.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class NodeAddress {
    String host;
    int port;
    public NodeAddress(String address){
        String[] value = address.split(":");
        host = value[0];
        port = Integer.parseInt(value[1]);
    }
}
