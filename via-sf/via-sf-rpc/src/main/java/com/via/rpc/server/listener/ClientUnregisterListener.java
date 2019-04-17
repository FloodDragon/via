package com.via.rpc.server.listener;

import com.via.rpc.remoting.Channel;

/**
 * Created by liuj-ai on 2019/1/23.
 */
public interface ClientUnregisterListener {
    void unregistered(String clientId, Channel channel);
}
