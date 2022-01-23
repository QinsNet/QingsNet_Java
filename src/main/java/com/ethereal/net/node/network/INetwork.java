package com.ethereal.net.node.network;

import java.net.MalformedURLException;

public interface INetwork {
    boolean start();
    boolean send(Object data);
    boolean close();
}
