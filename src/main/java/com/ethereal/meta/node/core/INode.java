package com.ethereal.meta.node.core;

public interface INode {

    public boolean start();
    public boolean send(Object data);
    public boolean close();
}
