package mt.server;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.TrackException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.meta.annotation.field.Field;
import com.qins.net.meta.annotation.instance.Meta;
import com.qins.net.meta.annotation.serialize.ReceiveAsync;
import com.qins.net.meta.annotation.serialize.ReceiveSync;
import com.qins.net.meta.annotation.serialize.SendAsync;
import com.qins.net.meta.annotation.serialize.SendSync;
import com.qins.net.node.annotation.Post;
import com.qins.net.node.util.NodeUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Meta(value = "User",nodes = "Shanghai")
public abstract class User{
    @Field
    private String username;
    @Field
    private String password;
    @Field
    private Integer apiToken;
    @Field
    private ArrayList<Package> packages;
    @Field
    Package aPackage;

    @Post
    @SendSync("{packages}")
    public Boolean addPack(@SendAsync("{name}") Package aPackage){
        this.aPackage = aPackage;
        aPackage.setName("改名字不会传递回请求方");
        packages.add(aPackage);
        return true;
    }

    @Post(nodes = "Shanghai")
    @SendSync("{packages}")
    public void hello(){
        for (Package item : packages){
            item.pack();
        }
    }

    @Post
    @SendSync("{password,apiToken}")
    public boolean login() {
        if("m839336369".equals(username) && "password".equals(password)){
            this.apiToken = 1234;
            this.password = "***";
            return true;
        }
        else return false;
    }

    @Post(nodes = {"Server_2","Server1"})
    @SendSync("{packages}")
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
