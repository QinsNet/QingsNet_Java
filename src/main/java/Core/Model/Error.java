package Core.Model;

public class Error {
    public enum ErrorCode{Intercepted,NotFoundService,NotFoundMethod,NotFoundNet,BufferFlow,Common,MaxConnects}
    public ErrorCode code;
    public String message;
    public String data;

    public ErrorCode getCode() {
        return code;
    }

    public void setCode(ErrorCode code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Error(ErrorCode code, String message, String data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    @Override
    public String toString() {
        return "Error{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
