package Net.Event.Delegate;

import sun.management.MethodInfo;

public interface InterceptorDelegate {
    boolean Interceptor(Service service, MethodInfo method);
}
