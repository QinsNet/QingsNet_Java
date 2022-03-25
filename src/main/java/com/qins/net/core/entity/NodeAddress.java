package com.qins.net.core.entity;

import com.qins.net.meta.annotation.serialize.Sync;
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
    @Sync
    String host;
    @Sync
    int port;
    public NodeAddress(String address){
        String[] value = address.split(":");
        host = value[0];
        port = Integer.parseInt(value[1]);
    }
}
