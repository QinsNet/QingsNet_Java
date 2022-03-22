package mt.server;

import com.qins.net.meta.annotation.field.Sync;
import com.qins.net.meta.annotation.instance.Meta;
import com.qins.net.node.annotation.Post;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Meta(value = "Package",nodes = "User")
@Accessors(chain = true)
public abstract class Package {
    @Sync
    String name;
    @Sync
    User user;
    @Post
    public abstract void pack();

}
