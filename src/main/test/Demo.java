import Model.User;
import RequestDemo.ClientRequest;
import ServiceDemo.ServerService;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Net.NetCore;
import com.ethereal.server.Net.WebSocket.WebSocketNet;
import com.ethereal.server.Request.RequestCore;
import com.ethereal.server.Server.Abstract.Server;
import com.ethereal.server.Server.ServerCore;
import com.ethereal.server.Server.WebSocket.WebSocketServer;
import com.ethereal.server.Service.Abstract.Service;
import com.ethereal.server.Service.ServiceCore;
import org.javatuples.Pair;

import java.util.ArrayList;

public class Demo {
    public static void main(String[] args) throws Exception {
        //单节点
        single("127.0.0.1:28015/NetDemo/","1");
        //分布式
//        ArrayList<String> arrayList = new ArrayList<>();
//        arrayList.add("127.0.0.1:28015/NetDemo/");
//        arrayList.add("127.0.0.1:28016/NetDemo/");
//        arrayList.add("127.0.0.1:28017/NetDemo/");
//        arrayList.add("127.0.0.1:28018/NetDemo/");
//        netNode("demo",arrayList);
    }
    public static void single(String prefixes,String netName) throws Exception {
        Net net = NetCore.register(new WebSocketNet(netName));
        net.getExceptionEvent().register(exception -> System.out.println(exception.getException().getMessage()));
        net.getLogEvent().register(log -> System.out.println(log.getMessage()));
        //向网关注册服务
        ServerService service = ServiceCore.register(net,new ServerService());
        //向网关注册请求
        ClientRequest clientRequest = RequestCore.register(net, ClientRequest.class);
        service.userRequest = clientRequest;
        Server server = ServerCore.register(net,new WebSocketServer(new ArrayList<>(), User::new));
        server.getPrefixes().add(prefixes);
        //关闭分布式
        net.getConfig().setNetNodeMode(false);
        //启动服务
        net.publish();
        server.getListenerSuccessEvent().register((value)->System.out.println(value.getPrefixes() + "启动成功"));
    }
    public static void netNode(String netName,ArrayList<String> prefixes) throws Exception {

    }
}
