package server;

import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.service.annotation.MetaService;
import com.google.gson.annotations.Expose;

public class Player {
    @Expose
    private String name;
    @Expose
    private Long id;
    @Expose
    @MetaMapping("package")
    Package aPackage;

    @MetaService("hello")
    public String hello(){
        aPackage.setName("背包1号");
        return String.format("[ID:%s]：您好，我是%s", id,name);
    }
}
