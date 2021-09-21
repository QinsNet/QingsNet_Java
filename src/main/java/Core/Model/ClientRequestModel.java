package Core.Model;

import java.util.Arrays;

public class ClientRequestModel {
    private ClientResponseModel result;
    private String type = "ER-1.0-ClientRequest";
    private String methodId;
    private Object[] params;
    private String id;
    private String service;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMethodId() {
        return methodId;
    }

    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public ClientRequestModel(String type, String methodId, Object[] params, String service) {
        this.type = type;
        this.methodId = methodId;
        this.params = params;
        this.service = service;
    }
    public void setResult(ClientResponseModel Result) {
        synchronized (this){
            result = Result;
            this.notify();
        }
    }

    public ClientResponseModel getResult(int timeout)  {
        synchronized (this){
            if (result == null){
                try {
                    if(timeout == -1)this.wait();
                    else this.wait(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return result;
        }
    }

    @Override
    public String toString() {
        return "ClientRequestModel{" +
                "result=" + result +
                ", type='" + type + '\'' +
                ", methodId='" + methodId + '\'' +
                ", params=" + Utils.Utils.gson.toJson(params) +
                ", service='" + service + '\'' +
                '}';
    }
}
