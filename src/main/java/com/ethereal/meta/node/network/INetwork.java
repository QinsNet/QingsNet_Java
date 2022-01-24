package com.ethereal.meta.node.network;

public interface INetwork {
    boolean start();
    boolean send(Object data);
    boolean close();
}
