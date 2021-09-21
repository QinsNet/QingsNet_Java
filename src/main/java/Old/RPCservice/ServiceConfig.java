package Old.RPCservice;

import Model.RPCTypeConfig;

public class ServiceConfig {
    private RPCTypeConfig types;

    public ServiceConfig(RPCTypeConfig types) {
        this.types = types;
    }

    public RPCTypeConfig getTypes() {
        return types;
    }

    public void setTypes(RPCTypeConfig types) {
        this.types = types;
    }
}
