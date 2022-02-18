package client;

import com.ethereal.meta.service.annotation.MetaService;

public class Package {
    String id;

    @MetaService("hello")
    public void hello(Integer id){
        System.out.printf("背包:%s%n", id);
    }
}
