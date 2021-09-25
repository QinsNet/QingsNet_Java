package Service.WebSocket;

import Core.Model.AbstractType;
import Core.Model.TrackException;
import Server.Abstract.Token;
import Service.Abstract.ServiceConfig;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class WebSocketService extends Service.Abstract.Service{

    public WebSocketServiceConfig getConfig() {
        return (WebSocketServiceConfig) config;
    }

    @Override
    public void Register(String netName, String service_name, Object instance, ServiceConfig config) throws TrackException {
        this.config = config;
        this.instance = instance;
        this.netName = netName;
        this.name = service_name;
        StringBuilder methodId = new StringBuilder();

        for(Method method : instance.getClass().getMethods()){
            int modifier = method.getModifiers();
            Service.Annotation.Service annotation = method.getAnnotation(Service.Annotation.Service.class);
            if(annotation != null)
            {
                if(!Modifier.isInterface(modifier)){
                    methodId.append(method.getName());
                    if(annotation.parameters().length == 0){
                        Class<?>[] parameters = method.getParameterTypes();
                        int start_idx = 1;
                        if(parameters.length>0 && (Token.class.isAssignableFrom(parameters[0]))){
                            start_idx = 0;
                        }
                        for(int i = start_idx;i<parameters.length;i++){
                            AbstractType rpcType = config.getTypes().getTypesByType().get(parameters[i]);
                            if(rpcType != null) {
                                methodId.append("-").append(rpcType.getName());
                            }
                            else throw new TrackException(TrackException.ErrorCode.Runtime,String.format("Java中的%s类型参数尚未注册,请注意是否是泛型导致！",parameters[i].getName()));
                        }
                    }
                    else {
                        String[] types_name = annotation.parameters();
                        for(String type_name : types_name){
                            if(config.getTypes().getTypesByName().containsKey(type_name)){
                                methodId.append("-").append(type_name);
                            }
                            else throw new TrackException(TrackException.ErrorCode.Runtime,String.format("Java中的%s抽象类型参数尚未注册,请注意是否是泛型导致！",type_name));
                        }
                    }
                    methods.put(methodId.toString(),method);
                    methodId.setLength(0);
                }
            }
        }
    }
}


