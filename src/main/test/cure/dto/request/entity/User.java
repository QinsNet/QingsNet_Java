package cure.dto.request.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    //用户信息
    protected String username;
    protected String password;
    protected String apiToken;
}
