package ServiceDemo;

import Model.User;
import RequestDemo.ClientRequest;
import com.ethereal.meta.core.aop.annotation.AfterEvent;
import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.service.annotation.Token;
import com.ethereal.meta.service.annotation.ServiceMapping;
import com.ethereal.meta.service.WebSocket.WebSocketService;

public class ServerService extends WebSocketService {

    public ServerService() throws TrackException {
        name = "Server";
        types.add(Integer.class,"Int");
        types.add(Long.class,"Long");
        types.add(String.class,"String");
        types.add(Boolean.class,"Bool");
        types.add(User.class,"User");
        createMethod = User::new;
    }
    public ClientRequest userRequest;
    @ServiceMapping(mapping = "Register")
    public Boolean Register(@Token User user,String username, Long id){
        user.setUsername(username);
        user.setId(id);
        return user.Register();
    }
    @ServiceMapping(mapping = "SendSay")
    public Boolean SendSay(@Token User sender, Long listener_id, String message){
        User listener = sender.GetToken(listener_id);
        if(listener!= null){
            userRequest.Say(listener,sender,message);
        }
        return false;
    }
    @ServiceMapping(mapping = "Add")
    public Integer Add(Integer a, Integer b){
        return a+b;
    }
    @ServiceMapping(mapping = "Login")
    public Boolean Login(String username,String password){
        System.out.println(username + ":" + password);
        return true;
    }
    @ServiceMapping(mapping = "test")
    @AfterEvent(function = "instance.after(ddd:d,s:s)")
    public Boolean test(String s,Integer d,Integer k){
        System.out.println("test");
        return true;
    }
    @Override
    public void initialize() throws TrackException {
        iocManager.register("instance",new EventClass());
    }

    @Override
    public void register() {

    }

    @Override
    public void unregister() {

    }

    @Override
    public void unInitialize() {

    }

}
