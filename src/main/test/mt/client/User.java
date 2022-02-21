package mt.client;

import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.request.annotation.MetaRequest;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public abstract class User{
    @Expose
    private String username;
    @Expose
    private String password;
    @Expose
    private Integer apiToken;
    @Expose
    @MetaMapping(value = "package",elementClass = Package.class)
    private ArrayList<Package> packages;
    @MetaRequest("login")
    public abstract boolean login();
    @MetaRequest("getPack")
    public abstract boolean getPack();
}
