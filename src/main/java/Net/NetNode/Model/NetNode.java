package Net.NetNode.Model;

import java.util.HashMap;

public class NetNode {
    //Net节点名
    private String name;
    //连接数量
    private long connects;
    /// <summary>
    /// 服务信息
    /// </summary>
    private HashMap<String, ServiceNode> services = new HashMap<>();
    /// <summary>
    /// 接口信息
    /// </summary>
    private HashMap<String, RequestNode> requests = new HashMap<>();
    /// <summary>
    /// 前缀
    /// </summary>
    private String[] prefixes;
    /// <summary>
    /// 硬件信息
    /// </summary>
    private HardwareInformation hardwareInformation;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getConnects() {
        return connects;
    }

    public void setConnects(long connects) {
        this.connects = connects;
    }

    public HashMap<String, ServiceNode> getServices() {
        return services;
    }

    public void setServices(HashMap<String, ServiceNode> services) {
        this.services = services;
    }

    public HashMap<String, RequestNode> getRequests() {
        return requests;
    }

    public void setRequests(HashMap<String, RequestNode> requests) {
        this.requests = requests;
    }

    public String[] getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(String[] prefixes) {
        this.prefixes = prefixes;
    }

    public HardwareInformation getHardwareInformation() {
        return hardwareInformation;
    }

    public void setHardwareInformation(HardwareInformation hardwareInformation) {
        this.hardwareInformation = hardwareInformation;
    }
}
