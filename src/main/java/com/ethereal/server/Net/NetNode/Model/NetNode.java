package com.ethereal.server.Net.NetNode.Model;

import com.google.gson.annotations.Expose;

import java.util.HashMap;

public class NetNode {
    /// <summary>
    /// Net节点名
    /// </summary>
    @Expose
    private String name;
    /// <summary>
    /// 连接数量
    /// </summary>
    @Expose
    private long connects;
    /// <summary>
    /// ip地址
    /// </summary>
    @Expose
    private String[] prefixes;
    /// <summary>
    /// 硬件信息
    /// </summary>
    @Expose
    private HardwareInformation hardwareInformation;
    /// <summary>
    /// 服务信息
    /// </summary>
    @Expose
    private HashMap<String, ServiceNode> services = new HashMap<>();
    /// <summary>
    /// 接口信息
    /// </summary>
    @Expose
    private HashMap<String, RequestNode> requests = new HashMap<>();

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

    public String[] getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(String[] prefixes) {
        this.prefixes = prefixes;
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

    public HardwareInformation getHardwareInformation() {
        return hardwareInformation;
    }

    public void setHardwareInformation(HardwareInformation hardwareInformation) {
        this.hardwareInformation = hardwareInformation;
    }
}
