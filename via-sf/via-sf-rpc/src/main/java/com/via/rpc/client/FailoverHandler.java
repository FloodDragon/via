package com.via.rpc.client;

import com.via.rpc.remoting.Client;

/**
 * Created by liuj-ai on 2019/1/28.
 */
public interface FailoverHandler {

    void failover(Client client);
}
