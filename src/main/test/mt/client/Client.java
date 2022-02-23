package mt.client;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.entity.NodeAddress;

public class Client {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        User user = MetaApplication.run(Root.class,"client.yaml").create("/user",new NodeAddress("localhost:28003"));
        user.setUsername("m839336369");
        user.setPassword("password");
        //user.login();
        user.getPack();
        user.getPackages().get(1).pack();
    }
}
