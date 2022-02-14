package com.ethereal.meta.net.p2p.sender;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.net.SocketAddress;

@Getter
@Setter
@AllArgsConstructor
public class RemoteInfo {
    SocketAddress remote;
}
