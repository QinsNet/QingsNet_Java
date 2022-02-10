package client;

import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.request.annotation.Request;
import com.ethereal.meta.service.annotation.Service;

@MetaMapping("/player")
public class Player {
    private String name;
    private Long id;
    @MetaMapping("/package")
    Package aPackage;

    @Request("/hello")
    public void hello(){

    }
}
