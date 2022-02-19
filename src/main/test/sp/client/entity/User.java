package sp.client.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User{
    private String username;
    private String password;
    private Integer apiToken;
    private Package aPackage;
}
