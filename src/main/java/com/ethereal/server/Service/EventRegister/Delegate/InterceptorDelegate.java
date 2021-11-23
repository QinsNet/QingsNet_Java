package com.ethereal.server.Service.EventRegister.Delegate;

import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Service.Abstract.Token;
import com.ethereal.server.Service.Abstract.Service;

import java.lang.reflect.Method;

public interface InterceptorDelegate {
    boolean onInterceptor(Net net, Service service, Method method, Token token);
}
