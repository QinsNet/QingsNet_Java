package server;

import com.ethereal.meta.request.annotation.MetaRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Package {
    String id;

    @MetaRequest("hello")
    public void hello(){

    }

}
