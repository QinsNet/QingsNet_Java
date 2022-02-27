package mt.server;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.TrackException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.meta.annotation.Meta;
import com.qins.net.node.util.NodeUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Meta
public abstract class User{
    @Meta
    private String username;
    @Meta
    private String password;
    @Meta
    private Integer apiToken;
    @Meta
    private ArrayList<Package> packages;

    @Meta("Server_1")
    public boolean addPack(@Meta Package aPackage, @Meta ArrayList<String> hash){
        hash.add("nihao");
        packages.add(aPackage);
        return true;
    }

    @Meta("Server_2")
    public void hello(){
        for (Package item : packages){
            item.pack();
        }
    }

    @Meta("Server_2")
    public boolean login() throws NewInstanceException {
        if("m839336369".equals(username) && "password".equals(password)){
            this.apiToken = 1234;
            Package aPackage = MetaApplication.create(Package.class);
            aPackage.pack();
            return true;
        }
        else return false;
    }

    @Meta(nodes = {"Server_2","Server1"})
    public boolean newPack(){
        try {
            Package aPackage = MetaApplication.create(Package.class);
            NodeUtil.copyNodeAll(this,aPackage);
            aPackage.setName("A背包");
            Package bPackage = MetaApplication.create(Package.class);
            NodeUtil.copyNodeAll(this,bPackage);
            bPackage.setName("B背包");
            packages = new ArrayList<>();
            packages.add(aPackage);
            packages.add(bPackage);
            return true;
        } catch (NewInstanceException | TrackException e) {
            e.printStackTrace();
            return false;
        }
    }
}
