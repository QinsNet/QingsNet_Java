package mt.server;

import com.qins.net.service.annotation.MetaService;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Package {
    @Expose
    String name;
    @MetaService("pack")
    public void pack(){
        System.out.println(name);;//输出背包Name
    }
}
