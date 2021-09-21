package Old.Model;

import Model.Error;

public class ClientResponseModel {
    private String JsonRpc = null;
    private Object Result = null;
    private Model.Error Error =null;
    private String Id = null;
    private String Service;
    private String  ResultType;

    public String getJsonRpc() {
        return JsonRpc;
    }

    public void setJsonRpc(String jsonRpc) {
        JsonRpc = jsonRpc;
    }

    public Object getResult() {
        return Result;
    }

    public void setResult(Object result) {
        Result = result;
    }

    public Model.Error getError() {
        return Error;
    }

    public void setError(Model.Error error) {
        Error = error;
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

    public String getResultType() {
        return ResultType;
    }

    public void setResultType(String resultType) {
        ResultType = resultType;
    }

    public ClientResponseModel(String jsonRpc, Object result, Model.Error error, String id, String service, String resultType) {
        JsonRpc = jsonRpc;
        Result = result;
        Error = error;
        Id = id;
        Service = service;
        ResultType = resultType;
    }

    @Override
    public String toString() {
        return "ClientResponseModel{" +
                "JsonRpc='" + JsonRpc + '\'' +
                ", Result=" + Result +
                ", Id='" + Id + '\'' +
                '}';
    }
}
