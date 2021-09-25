package Net;

import Net.Abstract.Net;
import Net.Abstract.NetConfig;

import java.util.HashMap;

public class NetCore {
    public static HashMap<String, Net> nets = new HashMap<String, Net>();

    public static Net get(String name)
    {
        return nets.get(name);
    }

    public static Net Register(String name, Net.NetType netType){
        return Register(name,netType);
    }
    public static Net Register(String name, NetConfig netConfig, Net.NetType netType){
        if(netConfig == null){
            throw new ArgumentNullException(nameof(config));
        }
    }

}
