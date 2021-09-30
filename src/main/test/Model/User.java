package Model;

import com.ethereal.server.Server.WebSocket.WebSocketBaseToken;
import com.google.gson.annotations.Expose;

public class User extends WebSocketBaseToken {
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

    @Override
    public Object getKey() {
        return id;
    }

    @Override
    public void setKey(Object key) {
        id = (int)key;
    }
}
