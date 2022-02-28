package com.qins.net.request.cglib;

import com.qins.net.core.entity.NodeAddress;
import com.qins.net.request.core.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class RequestInterceptor implements MethodInterceptor {
    @Getter
    @Setter
    private Request request;
    @Getter
    @Setter
    private Map<String,String> nodes;
    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Exception {
        return request.intercept(o,method,args,nodes);
    }
    public RequestInterceptor(Request request){
        this.request = request;
    }
}
