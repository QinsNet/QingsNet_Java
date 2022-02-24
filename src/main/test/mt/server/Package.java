package mt.server;

import com.google.gson.annotations.Expose;
import com.qins.net.meta.annotation.Meta;
import com.qins.net.meta.annotation.Sync;
import com.qins.net.node.annotation.PostMapping;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Package {
    @Sync
    String name;
    @PostMapping("pack")
    public void pack(){
        System.out.println(name);;//输出背包Name
    }
}
