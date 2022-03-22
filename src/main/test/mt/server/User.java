package mt.server;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.TrackException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.meta.annotation.field.Async;
import com.qins.net.meta.annotation.field.Sync;
import com.qins.net.meta.annotation.instance.Meta;
import com.qins.net.node.annotation.Post;
import com.qins.net.node.util.NodeUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Meta(value = "User",nodes = "Shanghai")
public abstract class User{
    @Sync
    private String username;
    @Async
    private String password;
    @Sync
    private Integer apiToken;
    @Sync
    private ArrayList<Package> packages;

    @Post
    public Boolean addPack(Package aPackage){
        packages.add(aPackage);
        return true;
    }

    @Post(nodes = "Shanghai")
    public void hello(){
        for (Package item : packages){
            item.pack();
        }
    }

    @Post
    public boolean login() throws NewInstanceException {
        if("m839336369".equals(username) && "password".equals(password)){
            this.apiToken = 1234;
            this.password = "***";
            return true;
        }
        else return false;
    }

    @Post(nodes = {"Server_2","Server1"})
    public boolean newPack(){
        try {
            Package aPackage = MetaApplication.create(Package.class);
            NodeUtil.copyNodeAll(this,aPackage);
            aPackage.setName("A背包");
            Package bPackage = MetaApplication.create(ServicePackage.class);
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
