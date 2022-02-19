package mt.server;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Package {
    String name;
    public void pack(){
        System.out.println(name);;//输出背包Name
    }
}
