package mt.client;

import com.qins.net.meta.annotation.Meta;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Meta(nodes = "User")
public abstract class Package {
    @Meta
    String name;
    @Meta
    public boolean pack(){
        if(name != null){
            System.out.println(name + " Pack!!!");
            return true;
        }
        return false;
    }
}
