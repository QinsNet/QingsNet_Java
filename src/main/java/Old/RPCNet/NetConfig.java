package Old.RPCNet;

import RPCNet.Event.Delegate.InterceptorDelegate;
import RPCNet.Event.InterceptorEvent;
import RPCservice.Service;
import sun.management.MethodInfo;

public class NetConfig {
    //网络拦截器事件
    public InterceptorEvent interceptorEvent;
    //分布式模式是否开启
    private boolean netNodeMode = false;
    //分布式ip组
    private List<Triplet<String,String,EtherealC.NativeClient.ClientConfig>> netNodeIps;
    //网络节点心跳周期
    private int netNodeHeartbeatCycle = 60000;

    public NetConfig() {
    }

    public InterceptorEvent getInterceptorEvent() {
        return interceptorEvent;
    }

    public void setInterceptorEvent(InterceptorEvent interceptorEvent) {
        this.interceptorEvent = interceptorEvent;
    }

    public boolean isNetNodeMode() {
        return netNodeMode;
    }

    public void setNetNodeMode(boolean netNodeMode) {
        this.netNodeMode = netNodeMode;
    }

    public List<Triplet<String, String, EtherealC.NativeClient.ClientConfig>> getNetNodeIps() {
        return netNodeIps;
    }

    public void setNetNodeIps(List<Triplet<String, String, EtherealC.NativeClient.ClientConfig>> netNodeIps) {
        this.netNodeIps = netNodeIps;
    }

    public int getNetNodeHeartbeatCycle() {
        return netNodeHeartbeatCycle;
    }

    public void setNetNodeHeartbeatCycle(int netNodeHeartbeatCycle) {
        this.netNodeHeartbeatCycle = netNodeHeartbeatCycle;
    }
    public boolean OnInterceptor(Service service, MethodInfo method) {
        if (interceptorEvent != null) {
            interceptorEvent.onEvent(service, method);
            return false;
        }
        return true;
    }
}
