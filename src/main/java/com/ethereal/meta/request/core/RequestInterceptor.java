package com.ethereal.meta.request.core;

import com.ethereal.meta.net.p2p.sender.RemoteInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

@AllArgsConstructor
public class RequestInterceptor implements MethodInterceptor {
    @Getter
    @Setter
    private Request request;
    @Getter
    @Setter
    private RemoteInfo remote;
    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) {
        return request.intercept(o,method,args,methodProxy, remote);
    }
}
