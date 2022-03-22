package mt.server;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.core.MetaClassLoader;

public class Server {
    public static void main(String[] args) throws LoadClassException {
        MetaApplication.run("server.yaml");
        MetaApplication.defineNode("Shanghai", "localhost:28003");
        MetaApplication.defineNode("Beijing", "localhost:28003");
        MetaApplication.publish(User.class);
    }
}
