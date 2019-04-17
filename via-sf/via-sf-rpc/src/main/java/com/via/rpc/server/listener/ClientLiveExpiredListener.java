package com.via.rpc.server.listener;

import com.via.rpc.remoting.Channel;

/**
 * Created by LiuJing on 17-1-17.
 */
public interface ClientLiveExpiredListener {
    void expired(String clientId, Channel channel);
}
