import Model.User;
import RequestDemo.ClientRequest;
import ServiceDemo.ServerService;
import com.ethereal.net.core.base.event.delegate.ExceptionEventDelegate;
import com.ethereal.net.core.entity.TrackException;
import com.ethereal.net.net.core.Net;
import com.ethereal.net.net.WebSocket.WebSocketNet;
import com.ethereal.net.node.network.http.server.core.Server;
import com.ethereal.net.node.network.http.server.WebSocket.WebSocketServer;

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
        service.userRequest = RequestCore.register(service, ClientRequest.class);
        Server server = ServerCore.register(net,new WebSocketServer(new ArrayList<>()));
        server.getPrefixes().add("ethereal://127.0.0.1:28015/NetDemo/".replace("28015",port));
        server.getListenerSuccessEvent().register((value)->System.out.println(value.getPrefixes() + "启动成功"));
        server.start();
    }
}
