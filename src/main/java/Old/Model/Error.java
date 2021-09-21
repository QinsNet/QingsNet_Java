package Old.Model;

public class Error {

    public enum ErrorCode{Intercepted}
    private  ErrorCode Code;
    private  String Message;
    private  String Data;

    public ErrorCode getCode() {
        return Code;
    }

    public void setCode(ErrorCode code) {
        Code = code;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getData() {
        return Data;
    }

    public void setData(String data) {
        Data = data;
    }

    @Override
    public String toString() {
        return "Error{" +
                "Code=" + Code +
                ", Message='" + Message + '\'' +
                ", Data='" + Data + '\'' +
                '}';
    }

    //    public ErrorCode Code{get;set}//错误代码
//    public String Message{get;set}//错误信息
//    public String Data{get;set}//绑定数据


}
