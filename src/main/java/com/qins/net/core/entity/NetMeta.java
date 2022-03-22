package com.qins.net.core.entity;

import com.qins.net.meta.annotation.field.Sync;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@Accessors(chain = true)
public class NetMeta {
    @Sync
    String instance;
    @Sync
    Map<String,String> nodes;
    public NetMeta(){

    }
}
