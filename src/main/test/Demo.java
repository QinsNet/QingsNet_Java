import Model.User;
import RequestDemo.ClientRequest;
import ServiceDemo.ServerService;
import com.ethereal.client.Client.Abstract.ClientConfig;
import com.ethereal.server.Core.Event.Delegate.ExceptionEventDelegate;
import com.ethereal.server.Core.Model.TrackException;
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
import java.util.Scanner;

public class Demo {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String  port = "28015";
        System.out.println("请选择端口(0-3):");
        port = scanner.next();
        switch (port){
            case "0":
                port = "28015";
                break;
            case "1":
                port = "28016";
                break;
            case "2":
                port = "28017";
                break;
            case "3":
                port = "28018";
                break;
        }
        Net net = NetCore.register(new WebSocketNet("demo"));
        net.getExceptionEvent().register(new ExceptionEventDelegate() {
            @Override
            public void onException(TrackException exception) {
                System.out.println(exception.getException().getMessage());
                exception.getException().printStackTrace();
            }
        });
        net.getLogEvent().register(log -> System.out.println(log.getMessage()));
        //向网关注册服务
        ServerService service = ServiceCore.register(net,new ServerService());
        //向网关注册请求
        service.userRequest = RequestCore.register(net, ClientRequest.class);
        Server server = ServerCore.register(net,new WebSocketServer(new ArrayList<>(), User::new));
        server.getPrefixes().add("127.0.0.1:28015/NetDemo/".replace("28015",port));
        //开启集群
        net.getConfig().setNetNodeMode(true);
        net.getConfig().setNetNodeIps(new ArrayList<>());
        net.getConfig().getNetNodeIps().add(new Pair<>("127.0.0.1:28015/NetDemo/",null));
        net.getConfig().getNetNodeIps().add(new Pair<>("127.0.0.1:28016/NetDemo/",null));
        net.getConfig().getNetNodeIps().add(new Pair<>("127.0.0.1:28017/NetDemo/",null));
        net.getConfig().getNetNodeIps().add(new Pair<>("127.0.0.1:28018/NetDemo/",null));
        //启动服务
        net.publish();
        server.getListenerSuccessEvent().register((value)->System.out.println(value.getPrefixes() + "启动成功"));
    }
}
