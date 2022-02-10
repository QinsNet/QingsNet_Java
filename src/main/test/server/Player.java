package server;

import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.service.annotation.Service;

@MetaMapping("/player")
public class Player {
    private String name;
    private Long id;
    @MetaMapping("/package")
    Package aPackage;

    @Service("/hello")
    public void hello(){
        aPackage.hello();
    }
}
