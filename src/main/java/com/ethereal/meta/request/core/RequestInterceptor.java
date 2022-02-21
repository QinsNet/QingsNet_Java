package com.ethereal.meta.request.core;

import com.ethereal.meta.core.entity.NodeAddress;
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
    private NodeAddress local;
    @Getter
    @Setter
    private NodeAddress remote;
    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Exception {
        return request.intercept(o,method,args,methodProxy, local,remote);
    }
}
