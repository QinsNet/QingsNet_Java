package mt.server;

import com.qins.net.meta.annotation.Meta;
import com.qins.net.node.annotation.PostMapping;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Meta
public abstract class Package {
    @Meta
    String name;
    @Meta
    public void pack(){
        System.out.println(name);;//输出背包Name
    }
}
