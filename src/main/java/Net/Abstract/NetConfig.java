package Net.Abstract;

import Net.Event.Delegate.InterceptorDelegate;
import Net.Event.InterceptorEvent;
import Net.Interface.INetConfig;

import Service.Abstract.Service;
import sun.management.MethodInfo;

public class NetConfig implements INetConfig {
   //分布模式是否开启
    private boolean netNodeMode = false;
    //分布式IP组
    private List<Pair<String,ClientConfig>> netNodeIps;
    //网络节点心跳周期
    private int netNodeHeartbeatCycle = 10000;
    //网络级拦截器事件
    public InterceptorEvent interceptorEvent ;
    

    public boolean isNetNodeMode() {
        return netNodeMode;
    }

    public void setNetNodeMode(boolean netNodeMode) {
        this.netNodeMode = netNodeMode;
    }

    public List<Pair<String, ClientConfig>> getNetNodeIps() {
        return netNodeIps;
    }

    public void setNetNodeIps(List<Pair<String, ClientConfig>> netNodeIps) {
        this.netNodeIps = netNodeIps;
    }

    public int getNetNodeHeartbeatCycle() {
        return netNodeHeartbeatCycle;
    }

    public void setNetNodeHeartbeatCycle(int netNodeHeartbeatCycle) {
        this.netNodeHeartbeatCycle = netNodeHeartbeatCycle;
    }
    public boolean OnInterceptor(Service service, MethodInfo method){
        if(interceptorEvent != null){
            for(InterceptorDelegate item:interceptorEvent.getListeners()){
                if(!item.invoke())
                    return false;
                return true;

            }
        }
    }

}
