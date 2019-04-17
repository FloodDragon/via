package com.via.light.rpc.client;

import com.via.light.rpc.remoting.Client;

/**
 * Created by liuj-ai on 2019/1/28.
 */
public interface FailoverHandler {

    void failover(Client client);
}
