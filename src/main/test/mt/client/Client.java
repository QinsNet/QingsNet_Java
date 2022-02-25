package mt.client;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.entity.NodeAddress;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.core.exception.NewInstanceException;

public class Client {
    public static void main(String[] args) throws LoadClassException, NewInstanceException {
        User user = MetaApplication.run("client.yaml").create(User.class,new NodeAddress("localhost:28001"));
        user.setUsername("m839336369");
        user.setPassword("password");
        user.login();
//        user.getPack();
//        user.getPackages().get(1).pack();
    }
}
