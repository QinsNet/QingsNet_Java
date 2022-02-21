package mt.server;

import com.ethereal.meta.request.annotation.MetaRequest;
import com.ethereal.meta.service.annotation.MetaService;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Package {
    @Expose
    String name;
    @MetaService("pack")
    public void pack(){
        System.out.println(name);;//输出背包Name
    }
}
