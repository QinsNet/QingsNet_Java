package server;

import com.ethereal.meta.core.boot.MetaApplication;
import com.ethereal.meta.node.p2p.recevier.Receiver;

public class Server {
    public static void main(String[] args) throws InterruptedException {
        MetaApplication application = MetaApplication.run(Root.class,"server.yaml");
        ((Receiver)application.getContext().getServer()).getChannel().closeFuture().sync();
    }
}
