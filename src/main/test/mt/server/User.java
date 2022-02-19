package mt.server;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User{
    private String username;
    private String password;
    private Integer apiToken;
    private Package aPackage;
    public boolean login(){
        if("m839336369".equals(username) && "password".equals(password)){
            this.apiToken = 1234;
            return true;
        }
        else return false;
    }
    public boolean getPack(){
        if(1234 == apiToken){
            Package aPackage = new Package();
            aPackage.setName("A背包");
            this.aPackage = aPackage;
            return true;
        }
        return false;
    }
}
