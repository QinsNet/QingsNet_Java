package Old.Model;

import java.util.Arrays;

public class ClientRequestModel {
    private String JsonRpc;//Ethereal-RPC版本
    private String MethodId;//方法ID
    private Object[] Params;//方法参数
    private String Id;//请求ID
    private String Service;//请求服务

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
        return Params;
    }

    public void setParams(Object[] params) {
        Params = params;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getService() {
        return Service;
    }

    public void setService(String service) {
        Service = service;
    }

    public ClientRequestModel(String jsonRpc, String methodId, Object[] params, String service) {
        JsonRpc = jsonRpc;
        MethodId = methodId;
        Params = params;
        Service = service;
    }

    @Override
    public String toString() {
        return "ClientRequestModel{" +
                "JsonRpc='" + JsonRpc + '\'' +
                ", MethodId='" + MethodId + '\'' +
                ", Params=" + Arrays.toString(Params) +
                ", Service='" + Service + '\'' +
                '}';
    }
}
