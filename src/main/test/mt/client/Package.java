package mt.client;

import com.google.gson.annotations.Expose;
import com.qins.net.node.annotation.PostMapping;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Package {
    @Expose
    String name;

    @PostMapping("pack")
    public abstract void pack();
}
