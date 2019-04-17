package com.via.rpc.client;

/**
 * Created by liuj-ai on 2019/1/23.
 */
public interface HeartbeatPostMan {
    boolean deliver(String message);
}
