package Old.RPCRequest;

import Model.RPCTypeConfig;

public class RequestConfig {
    private RPCTypeConfig types;

    public RPCTypeConfig getTypes() {
        return types;
    }

    public void setTypes(RPCTypeConfig types) {
        this.types = types;
    }

    public RequestConfig(RPCTypeConfig types) {
        this.types = types;
    }
}
