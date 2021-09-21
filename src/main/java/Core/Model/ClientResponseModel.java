package Core.Model;

public class ClientResponseModel {
    private String type = "ER-1.0-ClientResponse";
    private Object result = null;
    private  Error error = null;
    //响应ID
    private String id = null;
    //响应服务
    private String Service;
    //响应结果值类型
    private String resultType;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getService() {
        return Service;
    }

    public void setService(String service) {
        Service = service;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public ClientResponseModel(Object result, Error error, String id, String service, String resultType) {
        this.result = result;
        this.error = error;
        this.id = id;
        Service = service;
        this.resultType = resultType;
    }

    @Override
    public String toString() {
        return "ClientResponseModel{" +
                "type='" + type + '\'' +
                ", result=" + result +
                ", id='" + id + '\'' +
                '}';
    }
}
