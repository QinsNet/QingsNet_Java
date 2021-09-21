package Service.WebSocket;

import Core.Model.AbstractType;
import Core.Model.TrackException;
import Service.Abstract.ServiceConfig;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class WebSocketService extends Service.Abstract.Service{

    public WebSocketServiceConfig getConfig() {
        return (WebSocketServiceConfig) config;
    }

    @Override
    public void Register(String netName, String service_name, Object instance, ServiceConfig config) {
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
                        for(Class<?> parameter_type : method.getParameterTypes()){
                            AbstractType rpcType = config.getTypes().getTypesByType().get(parameter_type);
                            if(rpcType != null) {
                                methodId.append("-").append(rpcType.getName());
                            }
                            else try {
                                throw new TrackException(TrackException.ErrorCode.Runtime,String.format("Java中的%s类型参数尚未注册,请注意是否是泛型导致！",parameter_type.getName()));
                            } catch (TrackException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else {
                        String[] types_name = annotation.parameters();
                        for(String type_name : types_name){
                            if(config.getTypes().getTypesByName().containsKey(type_name)){
                                methodId.append("-").append(type_name);
                            }
                            else try {
                                throw new TrackException(TrackException.ErrorCode.Runtime,String.format("Java中的%s抽象类型参数尚未注册,请注意是否是泛型导致！",type_name));
                            } catch (TrackException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    methods.put(methodId.toString(),method);
                    methodId.setLength(0);
                }
            }
        }
    }
}

}
