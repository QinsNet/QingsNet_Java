package ServiceDemo;

import Model.User;
import RequestDemo.ClientRequest;
import com.ethereal.server.Core.Model.AbstractTypes;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Request.Annotation.InvokeTypeFlags;
import com.ethereal.server.Request.Annotation.Request;
import com.ethereal.server.Service.Annotation.Service;
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
    @Service
    public Boolean Register(User user,String username, Long id){
        user.setUsername(username);
        user.setId(id);
        return user.Register();
    }
    @Service
    public Boolean SendSay(User sender, Long listener_id, String message){
        User listener = sender.GetToken(listener_id);
        if(listener!= null){
            userRequest.Say(listener,sender,message);
        }
        return false;
    }
    @Service
    public Integer Add(Integer a, Integer b){
        return a+b;
    }

}
