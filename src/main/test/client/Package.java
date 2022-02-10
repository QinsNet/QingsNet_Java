package client;


import com.ethereal.meta.request.annotation.Request;
import com.ethereal.meta.service.annotation.Service;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Package {
    String id;

    @Service("/hello")
    public void hello(){
        System.out.printf("背包:%s%n", id);
    }

}
