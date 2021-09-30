package com.ethereal.server.Request.Abstract;

import com.ethereal.server.Core.Model.*;
import com.ethereal.server.Request.Annotation.InvokeTypeFlags;
import com.ethereal.server.Server.Abstract.BaseToken;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Random;

public class RequestMethodInterceptor implements MethodInterceptor {
    private Request instance;
    private Random random = new Random();

    public Request getInstance() {
        return instance;
    }

    public void setInstance(Request instance) {
        this.instance = instance;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        com.ethereal.server.Request.Annotation.Request annotation = method.getAnnotation(com.ethereal.server.Request.Annotation.Request.class);
        Object localResult = null;
        if((annotation.invokeType() & InvokeTypeFlags.Local) == 0){
            StringBuilder methodId = new StringBuilder(method.getName());
            String[] obj = null;
            if(args == null)throw new TrackException(TrackException.ErrorCode.Runtime, String.format("{%s}-{%s}缺少首参BaseToken！", instance.name,methodId));
            obj = new String[args.length - 1];
            if(annotation.parameters().length == 0){
                Class<?>[] parameters = method.getParameterTypes();
                for(int i=1;i<args.length;i++){
                    AbstractType rpcType = instance.types.getTypesByType().get(parameters[i]);
                    if(rpcType != null) {
                        methodId.append("-").append(rpcType.getName());
                        obj[i - 1] = rpcType.getSerialize().Serialize(args[i]);
                    }
                    else throw new TrackException(TrackException.ErrorCode.Runtime,String.format("Java中的%s类型参数尚未注册！",parameters[i].getName()));
                }
            }
            else {
                String[] types_name = annotation.parameters();
                if(args.length == types_name.length){
                    for(int i=0;i<args.length;i++){
                        AbstractType rpcType = instance.types.getTypesByName().get(types_name[i]);
                        if(rpcType!=null){
                            methodId.append("-").append(rpcType.getName());
                            obj[i] = rpcType.getSerialize().Serialize(args[i]);
                        }
                        else throw new TrackException(TrackException.ErrorCode.Runtime,String.format("方法体%s中的抽象类型为%s的类型尚未注册！",method.getName(),types_name[i]));
                    }
                }
                else throw new TrackException(TrackException.ErrorCode.Runtime,String.format("方法体%s中RPCMethod注解与实际参数数量不符,@RPCRequest:%d个,Method:%d个",method.getName(),types_name.length,args.length));
            }
            ServerRequestModel request = new ServerRequestModel("2.0", methodId.toString(),obj,instance.name);
            if(args[0] != null && args[0] instanceof BaseToken){
                if(!((BaseToken) args[0]).getCanRequest()){
                    throw new TrackException(TrackException.ErrorCode.Runtime, String.format("{%s}-{%s}传递了非WebSocket协议的Token！", instance.name,methodId));
                }
                ((BaseToken) args[0]).sendServerRequest(request);
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
