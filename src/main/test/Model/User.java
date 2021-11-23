package Model;

import com.ethereal.server.Service.WebSocket.WebSocketToken;
import com.google.gson.annotations.Expose;

public class User extends WebSocketToken {
    @Expose
    private long id;
    @Expose
    private String username;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
