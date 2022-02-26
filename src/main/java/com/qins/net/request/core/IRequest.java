package com.qins.net.request.core;

import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.HashMap;

public interface IRequest{
    void receive(RequestContext context);
    Object intercept(Object instance, Method method, Object[] args, HashMap<String,String> nodes) throws Exception;
}
