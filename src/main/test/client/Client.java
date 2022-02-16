package client;

import com.ethereal.meta.core.boot.MetaApplication;

import java.net.InetSocketAddress;

public class Client {
    public static void main(String[] args) {
        MetaApplication.run(Root.class,"client.yaml");
        Player player = MetaApplication.publish("/player",new RemoteInfo(new InetSocketAddress("localhost",28000)));
        player.hello();
    }
}
