package mt.client;

import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.request.annotation.MetaRequest;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Package {
    @Expose
    String name;

    @MetaRequest("pack")
    public abstract void pack();
}
