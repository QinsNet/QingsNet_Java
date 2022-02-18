package server;

import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.service.annotation.MetaService;

public class Player {
    private String name;
    private Long id;
    @MetaMapping("package")
    Package aPackage;

    @MetaService("hello")
    public void hello(){
        aPackage.hello(123);
    }
}
