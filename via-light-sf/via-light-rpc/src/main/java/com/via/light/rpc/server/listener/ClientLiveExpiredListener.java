package com.via.light.rpc.server.listener;

import com.via.light.rpc.remoting.Channel;

/**
 * Created by LiuJing on 17-1-17.
 */
public interface ClientLiveExpiredListener {
    void expired(String clientId, Channel channel);
}
