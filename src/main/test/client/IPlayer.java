package client;

import com.ethereal.meta.request.annotation.MetaRequest;

public interface IPlayer {
    @MetaRequest("hello")
    String hello();
}
