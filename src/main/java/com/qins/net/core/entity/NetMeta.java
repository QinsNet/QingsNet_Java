package com.qins.net.core.entity;

import com.qins.net.meta.annotation.Meta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@Accessors(chain = true)
public class NetMeta {
    @Meta
    Object instance;
    @Meta
    Map<String,String> nodes;
    public NetMeta(){

    }
}
