package cure.bo.service.bo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User{
    //用户信息
    protected String username;
    protected String password;
    protected String apiToken;
    public boolean login(){
        if("839336369".equals(username) && "12345678".equals(password)){
            apiToken = "abc123";
            return true;
        }
        else return false;
    }
}
