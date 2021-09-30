package RequestDemo;


import Model.User;
import com.ethereal.server.Core.Model.AbstractTypes;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Request.Annotation.InvokeTypeFlags;
import com.ethereal.server.Request.Annotation.Request;
import com.ethereal.server.Request.WebSocket.WebSocketRequest;
import com.ethereal.server.Service.Annotation.Service;

public class ClientRequest extends WebSocketRequest {
    public ClientRequest() throws TrackException {
        super("Client", new AbstractTypes());
        types.add(Integer.class,"Int");
        types.add(Long.class,"Long");
        types.add(String.class,"String");
        types.add(Boolean.class,"Bool");
        types.add(User.class,"User");
    }
    @Request
    public void Say(User user, User sender, String message)
    {
        System.out.println(sender.getUsername() + ":" + message);
    }
}
