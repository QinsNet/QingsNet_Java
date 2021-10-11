package com.ethereal.server.Service.Event.Delegate;

import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Server.Abstract.Token;
import com.ethereal.server.Service.Abstract.Service;

import java.lang.reflect.Method;

public interface InterceptorDelegate {
    boolean onInterceptor(Net net, Service service, Method method, Token token);
}
