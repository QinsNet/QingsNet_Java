package mt.client;

import com.qins.net.meta.annotation.Meta;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Meta(value = "User",nodes = "Server_1")
public abstract class User {
    @Meta
    private String username;
    @Meta
    private String password;
    @Meta
    private Integer apiToken;
    @Meta
    private ArrayList<Package> packages;

    @Meta
    public abstract boolean login();

    @Meta
    public abstract boolean newPack();

    @Meta(nodes = {"Server_2", "Server1"})
    public abstract boolean addPack(@Meta Package aPackage);

    @Meta(nodes = "Server_2")
    public abstract void hello();
}