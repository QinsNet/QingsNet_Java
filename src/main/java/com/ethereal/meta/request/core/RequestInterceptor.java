package com.ethereal.meta.request.core;

import com.ethereal.meta.core.aop.annotation.ExceptionEvent;
import com.ethereal.meta.core.type.AbstractType;
import com.ethereal.meta.core.aop.annotation.AfterEvent;
import com.ethereal.meta.core.aop.annotation.BeforeEvent;
import com.ethereal.meta.core.aop.context.AfterEventContext;
import com.ethereal.meta.core.aop.context.BeforeEventContext;
import com.ethereal.meta.core.aop.context.EventContext;
import com.ethereal.meta.core.aop.context.ExceptionEventContext;
import com.ethereal.meta.core.entity.*;
import com.ethereal.meta.core.type.Param;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.net.network.INetwork;
import com.ethereal.meta.request.annotation.*;
import com.ethereal.meta.request.aop.annotation.FailEvent;
import com.ethereal.meta.request.aop.annotation.SuccessEvent;
import com.ethereal.meta.request.aop.annotation.TimeoutEvent;
import com.ethereal.meta.request.aop.context.FailEventContext;
import com.ethereal.meta.request.aop.context.SuccessEventContext;
import com.ethereal.meta.request.aop.context.TimeoutEventContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Random;

@AllArgsConstructor
public class RequestInterceptor implements MethodInterceptor {
    @Getter
    @Setter
    private Request request;
    @Getter
    @Setter
    private INetwork network;
    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) {
        return request.intercept(o,method,args,methodProxy,network);
    }
}
