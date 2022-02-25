package mt.client;

import com.qins.net.meta.annotation.Meta;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public abstract class User{
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
    public abstract boolean getPack();
}
