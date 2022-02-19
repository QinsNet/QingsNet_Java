package client;

import com.ethereal.meta.service.annotation.MetaService;
import com.google.gson.annotations.Expose;

public class Package {
    @Expose
    String name;

    public void pack(){
        System.out.printf("%s已经打包", name);
    }
}
