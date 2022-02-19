package server;

import com.ethereal.meta.request.annotation.MetaRequest;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Package {
    @Expose
    private String name;

}
