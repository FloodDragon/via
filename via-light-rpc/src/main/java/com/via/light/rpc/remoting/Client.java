
package com.via.light.rpc.remoting;

import com.via.light.rpc.client.HeartBeatState;
import com.via.light.rpc.client.HeartbeatPostMan;
import com.via.light.rpc.client.ServerTimestamp;
import com.via.light.rpc.conf.Config;

public interface Client extends Endpoint, Channel {

    Config getConfig();

    Channel getChannel();

    HeartbeatPostMan getHeartbeatPostMan();

    HeartBeatState getHeartBeatState();

    ServerTimestamp serverTimestamp();
}