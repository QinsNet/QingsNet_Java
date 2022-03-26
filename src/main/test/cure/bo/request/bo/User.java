package cure.bo.request.bo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class User{
    //用户信息
    protected String username;
    protected String password;
    protected String apiToken;

    public abstract boolean login();
}
