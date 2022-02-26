package mt.client;

import com.qins.net.meta.annotation.Meta;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Meta
public abstract class Package {
    @Meta
    String name;
    @Meta("User")
    public boolean pack(){
        if(name != null){
            System.out.println(name + " Pack!!!");
            return true;
        }
        return false;
    }
}
