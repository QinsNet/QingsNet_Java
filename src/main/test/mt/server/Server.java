package mt.server;

import com.ethereal.meta.core.boot.MetaApplication;

public class Server {
    public static void main(String[] args) {
        MetaApplication.run(Root.class,"server.yaml");
    }
}
