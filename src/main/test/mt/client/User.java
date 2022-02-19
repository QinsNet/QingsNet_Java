package mt.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class User{
    private String username;
    private String password;
    private Integer apiToken;
    private Package aPackage;
    public abstract boolean login();
    public abstract boolean getPack();
}
