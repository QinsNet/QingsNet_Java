package Service.Interface;

import Core.Interface.IExceptionEvent;
import Core.Interface.ILogEvent;
import Service.Abstract.ServiceConfig;

public interface IService extends ILogEvent, IExceptionEvent {
        public void Register(String netName, String service_name, Object instance, ServiceConfig config);

}
