package RequestDemo;


import Model.User;
import com.ethereal.net.core.entity.TrackException;
import com.ethereal.net.request.annotation.RequestMapping;
import com.ethereal.net.request.WebSocket.WebSocketRequest;

public class ClientRequest extends WebSocketRequest {
    public ClientRequest() throws TrackException {
        name="Client";
        types.add(Integer.class,"Int");
        types.add(Long.class,"Long");
        types.add(String.class,"String");
        types.add(Boolean.class,"Bool");
        types.add(User.class,"User");
    }
    @RequestMapping(mapping = "Say")
    public void Say(User user, User sender, String message)
    {
        System.out.println(sender.getUsername() + ":" + message);
    }

    @Override
    public void initialize() {

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
