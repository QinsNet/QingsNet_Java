package mt.server;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.meta.annotation.Meta;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Meta
public class User{
    @Meta
    private String username;
    @Meta
    private String password;
    @Meta
    private Integer apiToken;
    @Meta(element = Package.class)
    private ArrayList<Package> packages;
    @Meta
    public boolean login(){
        if("m839336369".equals(username) && "password".equals(password)){
            this.apiToken = 1234;
            return true;
        }
        else return false;
    }
    @Meta
    public boolean getPack(){
        try {
            Package aPackage = null;
            aPackage = MetaApplication.create(this, Package.class);
            aPackage.setName("A背包");
            Package bPackage = MetaApplication.create(this,Package.class);
            bPackage.setName("B背包");
            packages.add(aPackage);
            packages.add(bPackage);
            return true;
        } catch (LoadClassException | NewInstanceException e) {
            e.printStackTrace();
            return false;
        }
    }
}
