package mt.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Package {
    String name;
    public abstract void pack();
}
