package com.ethereal.meta.net.network;

public interface INetwork {
     boolean start();
     boolean send(Object data);
     boolean close();
}
