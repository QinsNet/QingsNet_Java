package sp.server.service;

import sp.server.entity.Package;
import sp.server.entity.User;

public class UserService {
    public int login(User user){
        if("m839336369".equals(user.getUsername()) && "password".equals(user.getPassword())){
            return 1234;//ApiToken
        }
        else return -1;
    }
    public Package getPack(int apiToken){
        if(1234 == apiToken){
            Package aPackage = new Package();
            aPackage.setName("A背包");
            return aPackage;
        }
        return null;
    }
}
