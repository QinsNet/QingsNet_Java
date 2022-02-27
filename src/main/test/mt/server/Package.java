package mt.server;

import com.qins.net.meta.annotation.Meta;
import com.qins.net.node.annotation.NodeMapping;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Meta
public abstract class Package {
    @Meta
    String name;
    @Meta("User")
    public abstract boolean pack();
}
