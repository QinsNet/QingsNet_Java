package client;

import com.ethereal.meta.core.boot.MetaApplication;
import com.ethereal.meta.core.entity.NodeAddress;
import com.ethereal.meta.util.SerializeUtil;
import com.google.gson.Gson;

import javax.sql.rowset.serial.SerialArray;

public class Client {
    public static void main(String[] args) {
        Player player = MetaApplication.run(Root.class,"client.yaml").create("/player",new NodeAddress("localhost:28003"));
        player.setId(1234L);
        System.out.println(player.hello());
        player.getAPackage().pack();
    }
}
