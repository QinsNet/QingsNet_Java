package com.ethereal.meta.net.network;

public interface Network {
     boolean start();
     boolean send(Object data);
     boolean close();
}
