package Old.Model;

import java.util.Arrays;

public class ServerRequestModel {
    private String JsonRpc;
    private String MethodId;
    private Object[] params;
    private String Service;

    public String getJsonRpc() {
        return JsonRpc;
    }

    public void setJsonRpc(String jsonRpc) {
        JsonRpc = jsonRpc;
    }

    public String getMethodId() {
        return MethodId;
    }

    public void setMethodId(String methodId) {
        MethodId = methodId;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public String getService() {
        return Service;
    }

    public void setService(String service) {
        Service = service;
    }

    public ServerRequestModel(String jsonRpc, String methodId, Object[] params, String service) {
        JsonRpc = jsonRpc;
        MethodId = methodId;
        this.params = params;
        Service = service;
    }

    @Override
    public String toString() {
        return "ServerRequestModel{" +
                "JsonRpc='" + JsonRpc + '\'' +
                ", MethodId='" + MethodId + '\'' +
                ", params=" + Arrays.toString(params) +
                ", Service='" + Service + '\'' +
                '}';
    }
}
