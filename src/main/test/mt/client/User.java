package mt.client;

import com.qins.net.meta.annotation.field.Async;
import com.qins.net.meta.annotation.field.Sync;
import com.qins.net.meta.annotation.instance.Meta;
import com.qins.net.node.annotation.Post;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Meta(nodes = "Shanghai")
public abstract class User {
    @Sync
    private String username;
    @Async
    private String password;
    @Sync
    private Integer apiToken;
    @Sync
    private ArrayList<Package> packages;

    @Post
    public abstract boolean login();

    @Post
    public abstract boolean newPack();

    @Post(nodes = {"Beijing", "Shanghai"})
    public abstract boolean addPack(Package aPackage);

    @Post(nodes = "Shanghai")
    public abstract void hello();
}