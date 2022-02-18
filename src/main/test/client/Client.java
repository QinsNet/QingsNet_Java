package client;

import com.ethereal.meta.core.boot.MetaApplication;
import com.ethereal.meta.core.entity.NodeAddress;

public class Client {
    public static void main(String[] args) {
        Player player = MetaApplication.run(Root.class,"client.yaml").publish("/player",new NodeAddress("localhost:28001"));
        player.hello();
    }
}
