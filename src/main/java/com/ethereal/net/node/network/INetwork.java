package com.ethereal.net.node.network;

public interface INetwork {
    boolean start();
    boolean send(Object data);
    boolean close();
}
