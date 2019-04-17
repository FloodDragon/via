
package com.via.rpc.remoting;

import com.via.rpc.client.HeartBeatState;
import com.via.rpc.client.HeartbeatPostMan;
import com.via.rpc.client.ServerTimestamp;
import com.via.rpc.conf.Config;

public interface Client extends Endpoint, Channel {

    Config getConfig();

    Channel getChannel();

    HeartbeatPostMan getHeartbeatPostMan();

    HeartBeatState getHeartBeatState();

    ServerTimestamp serverTimestamp();
}