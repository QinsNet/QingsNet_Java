package ServiceDemo;

import Model.User;
import RequestDemo.ClientRequest;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Server.Annotation.Token;
import com.ethereal.server.Service.Annotation.ServiceMethod;
import com.ethereal.server.Service.WebSocket.WebSocketService;

public class ServerService extends WebSocketService {

    public ServerService() throws TrackException {
        name = "Server";
        types.add(Integer.class,"Int");
        types.add(Long.class,"Long");
        types.add(String.class,"String");
        types.add(Boolean.class,"Bool");
        types.add(User.class,"User");
    }
    public ClientRequest userRequest;
    @ServiceMethod
    public Boolean Register(@Token User user,String username, Long id){
        user.setUsername(username);
        user.setId(id);
        return user.Register();
    }
    @ServiceMethod
    public Boolean SendSay(@Token User sender, Long listener_id, String message){
        User listener = sender.GetToken(listener_id);
        if(listener!= null){
            userRequest.Say(listener,sender,message);
        }
        return false;
    }
    @ServiceMethod
    public Integer Add(Integer a, Integer b){
        return a+b;
    }
    @ServiceMethod
    public Boolean Login(String username,String password){
        System.out.println(username + ":" + password);
        return true;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void unInitialize() {

    }
}
