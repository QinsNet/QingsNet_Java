package mt.server;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.meta.annotation.MetaMapping;
import com.qins.net.service.annotation.MetaService;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class User{
    @Expose
    private String username;
    @Expose
    private String password;
    @Expose
    private Integer apiToken;
    @Expose
    @MetaMapping(value = "package",elementClass = Package.class)
    private ArrayList<Package> packages;
    @MetaService("login")
    public boolean login(){
        if("m839336369".equals(username) && "password".equals(password)){
            this.apiToken = 1234;
            return true;
        }
        else return false;
    }
    @MetaService("getPack")
    public boolean getPack(){
        Package aPackage = MetaApplication.create(this,"/package");
        aPackage.setName("A背包");
        Package bPackage = MetaApplication.create(this,"/package");
        bPackage.setName("B背包");
        packages.add(aPackage);
        packages.add(bPackage);
        return true;
    }
}
