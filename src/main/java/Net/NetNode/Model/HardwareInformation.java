package Net.NetNode.Model;

public class HardwareInformation {
    /// <summary>
    /// 系统名称
    /// </summary>
    private String oSDescription;
    /// <summary>
    /// 系统架构
    /// </summary>
    private String oSArchitecture;
    /// <summary>
    /// 进程架构
    /// </summary>
    private String processArchitecture;
    /// <summary>
    /// 是否64位操作系统
    /// </summary>
    private String is64BitOperatingSystem;
    /// <summary>
    /// 网络接口信息
    /// </summary>
    private String networkInterfaces;

    public String getoSDescription() {
        return oSDescription;
    }

    public void setoSDescription(String oSDescription) {
        this.oSDescription = oSDescription;
    }

    public String getoSArchitecture() {
        return oSArchitecture;
    }

    public void setoSArchitecture(String oSArchitecture) {
        this.oSArchitecture = oSArchitecture;
    }

    public String getProcessArchitecture() {
        return processArchitecture;
    }

    public void setProcessArchitecture(String processArchitecture) {
        this.processArchitecture = processArchitecture;
    }

    public String getIs64BitOperatingSystem() {
        return is64BitOperatingSystem;
    }

    public void setIs64BitOperatingSystem(String is64BitOperatingSystem) {
        this.is64BitOperatingSystem = is64BitOperatingSystem;
    }

    public String getNetworkInterfaces() {
        return networkInterfaces;
    }

    public void setNetworkInterfaces(String networkInterfaces) {
        this.networkInterfaces = networkInterfaces;
    }
}
