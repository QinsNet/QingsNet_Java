package mt.client;

import com.qins.net.meta.annotation.field.Field;
import com.qins.net.meta.annotation.instance.Meta;
import com.qins.net.meta.annotation.serialize.ReceiveAsync;
import com.qins.net.meta.annotation.serialize.SendSync;
import com.qins.net.node.annotation.Post;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Meta(nodes = "Shanghai")
public abstract class User {
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
    @SendSync("{username,password}")
    public abstract boolean login();

    @Post
    @SendSync("{packages}")
    public abstract boolean newPack();

    @Post(nodes = {"Beijing", "Shanghai"})
    @SendSync("{packages}")
    public abstract boolean addPack(Package aPackage);

    @Post(nodes = "Shanghai")
    public abstract void hello();
}