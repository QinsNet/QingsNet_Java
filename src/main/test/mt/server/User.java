package mt.server;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.meta.annotation.Meta;
import com.qins.net.meta.annotation.Sync;
import com.qins.net.node.annotation.PostMapping;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class User{
    @Sync
    private String username;
    @Sync
    private String password;
    @Sync
    private Integer apiToken;
    @Sync
    @Meta(value = "package",elementClass = Package.class)
    private ArrayList<Package> packages;

    @PostMapping("login")
    public boolean login(){
        if("m839336369".equals(username) && "password".equals(password)){
            this.apiToken = 1234;
            return true;
        }
        else return false;
    }

    @PostMapping("getPack")
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
