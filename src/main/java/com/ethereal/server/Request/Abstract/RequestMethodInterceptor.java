package com.ethereal.server.Request.Abstract;

import com.ethereal.server.Core.Model.*;
import com.ethereal.server.Core.Model.Error;
import com.ethereal.server.Request.Annotation.InvokeTypeFlags;
import com.ethereal.server.Server.Abstract.Token;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Random;

public class RequestMethodInterceptor implements MethodInterceptor {
    private Random random = new Random();

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Request instance = (Request) o;
        com.ethereal.server.Request.Annotation.Request annotation = method.getAnnotation(com.ethereal.server.Request.Annotation.Request.class);
        Object localResult = null;
        if((annotation.invokeType() & InvokeTypeFlags.Local) == 0){
            Token token = null;
            StringBuilder methodId = new StringBuilder(method.getName());
            Parameter[] parameterInfos = method.getParameters();
            ArrayList<String> params = new ArrayList<>(parameterInfos.length - 1);
            for(int i = 0; i< parameterInfos.length; i++){
                if(parameterInfos[i].getAnnotation(com.ethereal.server.Server.Annotation.Token.class)!=null){
                    token = (Token) args[i];
                }
                else {
                    AbstractType type = instance.getTypes().getTypesByType().get(parameterInfos[i].getParameterizedType());
                    if(type == null)type = instance.getTypes().getTypesByName().get(method.getAnnotation(com.ethereal.server.Core.Annotation.AbstractType.class).abstractName());
                    if(type == null)throw new TrackException(TrackException.ErrorCode.Runtime,String.format("RPC中的%s类型参数尚未被注册！",parameterInfos[i].getParameterizedType()));
                    methodId.append("-").append(type.getName());
                    params.add(type.getSerialize().Serialize(args[i]));
                }
            }
            ServerRequestModel request = new ServerRequestModel("2.0", methodId.toString(),params.toArray(new String[]{ }),instance.name);
            if(token != null){
                if(!token.getCanRequest()){
                    throw new TrackException(TrackException.ErrorCode.Runtime, String.format("{%s}-{%s}传递了无法请求的Token！", instance.name,methodId));
                }
                token.sendServerRequest(request);
                if((annotation.invokeType() & InvokeTypeFlags.All) != 0){
                    localResult = methodProxy.invokeSuper(instance,args);
                }
            }
            else throw new TrackException(TrackException.ErrorCode.Runtime, String.format("{%s}-{%s}首参并非BaseToken实现类！", instance.name,methodId));
        }
        else {
            localResult = methodProxy.invokeSuper(instance,args);
        }
        return localResult;
    }
}
