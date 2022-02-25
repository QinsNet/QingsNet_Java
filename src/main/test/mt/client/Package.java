package mt.client;

import com.qins.net.meta.annotation.Meta;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Meta
public abstract class Package {
    @Meta
    String name;
    @Meta
    public abstract void pack();
}
