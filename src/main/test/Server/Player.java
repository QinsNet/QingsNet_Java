package Server;

import Model.User;
import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.meta.Meta;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player extends Meta {
    private String name;
    private Long id;
    public Player() throws TrackException {
        types.add("Int",Integer.class);
        types.add("Long",Long.class);
        types.add("String",String.class);
        types.add("Bool",Boolean.class);
    }

    @Override
    protected void onConfigure() {

    }

    @Override
    protected void onRegister() {

    }

    @Override
    protected void onInstance() {

    }

    @Override
    protected void onLink() {

    }

    @Override
    protected void onInitialize() {

    }

    @Override
    protected void onUninitialize() {

    }
}
