package client;

import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.request.annotation.MetaRequest;

public class Player {
    private String name;
    private Long id;
    @MetaMapping("package")
    Package aPackage;

    @MetaRequest("hello")
    public String hello(){
        return "";
    }
}
