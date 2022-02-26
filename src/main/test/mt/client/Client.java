package mt.client;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.entity.NodeAddress;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.core.exception.NewInstanceException;

public class Client {
    public static void main(String[] args) throws NewInstanceException {
        MetaApplication application = MetaApplication
                .run("client.yaml")
                .defineNode("User","localhost:28017")
                .defineNode("Server_1","localhost:28003")
                .defineNode("Server_2","localhost:28003");
        User user = application.create(User.class);
        user.setUsername("m839336369");
        user.setPassword("password");
        user.newPack();
        System.out.println("123");
    }
}
