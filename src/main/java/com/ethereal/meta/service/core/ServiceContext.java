package com.ethereal.meta.service.core;

import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.net.p2p.sender.RemoteInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class ServiceContext {
    private Object instance;
    private RequestMeta requestMeta;
    private HashMap<String,Object> params;
    private RemoteInfo remoteInfo;
}
