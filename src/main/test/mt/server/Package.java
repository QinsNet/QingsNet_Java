package mt.server;

import com.qins.net.meta.annotation.Meta;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Meta
@Accessors(chain = true)
public abstract class Package {
    @Meta
    String name;
    @Meta(nodes = "User")
    public abstract boolean pack();
}
