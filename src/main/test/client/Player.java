package client;

import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.request.annotation.MetaRequest;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Player implements IPlayer{
    @Expose
    private String name = "客户端";
    @Expose
    private Long id;
    @Expose
    @MetaMapping("package")
    Package aPackage;
}
