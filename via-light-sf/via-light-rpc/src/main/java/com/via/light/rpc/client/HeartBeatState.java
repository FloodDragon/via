package com.via.light.rpc.client;

/**
 * Created by liuj-ai on 2019/1/23.
 */
public interface HeartBeatState {

    enum State {
        BORN,
        HEALTHY,
        RECOVERY,
        CEASE;
    }

    State getState();
}
