package mt.server;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.LoadClassException;

public class Server {
    public static void main(String[] args) throws LoadClassException {
        MetaApplication.run("server.yaml")
                .publish(User.class);
    }
}
