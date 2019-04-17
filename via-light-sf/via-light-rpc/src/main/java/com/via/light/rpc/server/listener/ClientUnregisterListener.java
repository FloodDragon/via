package com.via.light.rpc.server.listener;

import com.via.light.rpc.remoting.Channel;

/**
 * Created by liuj-ai on 2019/1/23.
 */
public interface ClientUnregisterListener {
    void unregistered(String clientId, Channel channel);
}
