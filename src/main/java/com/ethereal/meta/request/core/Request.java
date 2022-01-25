package com.ethereal.meta.request.core;

import com.ethereal.meta.core.entity.*;
import com.ethereal.meta.meta.RawMeta;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;



@com.ethereal.meta.request.annotation.Request
public abstract class Request extends RawMeta implements IRequest {
    @Getter
    protected final ConcurrentHashMap<Integer, com.ethereal.meta.core.entity.RequestMeta> tasks = new ConcurrentHashMap<>();
    @Getter
    protected RequestConfig requestConfig;

}
