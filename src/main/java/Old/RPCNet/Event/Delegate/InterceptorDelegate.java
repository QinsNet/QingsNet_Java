package Old.RPCNet.Event.Delegate;

import RPCservice.Service;
import sun.management.MethodInfo;

public interface InterceptorDelegate {
    boolean Interceptor(Service service, MethodInfo method);

}
