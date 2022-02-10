package server;


import com.ethereal.meta.request.annotation.Request;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Package {
    String id;

    @Request("/hello")
    public void hello(){

    }

}
