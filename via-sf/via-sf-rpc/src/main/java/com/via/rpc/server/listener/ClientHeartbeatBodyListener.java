package com.via.rpc.server.listener;

/**
 * Created by liuj-ai on 2019/1/23.
 */
public interface ClientHeartbeatBodyListener {
    void process(String clientId, String body);
}
