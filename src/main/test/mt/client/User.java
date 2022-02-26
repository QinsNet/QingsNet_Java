package mt.client;

import com.google.gson.reflect.TypeToken;
import com.qins.net.meta.annotation.Meta;
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
    @Meta(element = Package.class)
    private ArrayList<Package> packages;
    @Meta("Server_1")
    public abstract boolean login();
    @Meta("Server_2")
    public abstract boolean newPack();
    @Meta(nodes = {"Server_2","Server1"})
    public abstract boolean addPack(Package aPackage);
}
