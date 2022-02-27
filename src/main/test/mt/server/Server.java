package mt.server;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.LoadClassException;

public class Server {
    public static void main(String[] args) throws LoadClassException {
        MetaApplication.run("server.yaml");
        MetaApplication.defineNode("User", "localhost:28017");
        MetaApplication.defineNode("Server_1", "localhost:28003");
        MetaApplication.defineNode("Server_2", "localhost:28003");
        MetaApplication.publish(User.class);
    }
}
