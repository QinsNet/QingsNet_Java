package mt.client;

import com.qins.net.meta.annotation.Meta;
import com.qins.net.meta.annotation.Sync;
import com.qins.net.node.annotation.PostMapping;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public abstract class User{
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
    public abstract boolean login();
    @PostMapping("getPack")
    public abstract boolean getPack();
}
