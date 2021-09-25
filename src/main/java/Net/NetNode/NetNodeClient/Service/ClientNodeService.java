package Net.NetNode.NetNodeClient.Service;

import Net.NetNode.NetNodeClient.Request.ServerNodeRequest;

public class ClientNodeService {
    private ServerNodeRequest serverNodeRequest;

    public ServerNodeRequest getServerNodeRequest() {
        return serverNodeRequest;
    }

    public void setServerNodeRequest(ServerNodeRequest serverNodeRequest) {
        this.serverNodeRequest = serverNodeRequest;
    }
}
