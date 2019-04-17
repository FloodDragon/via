package com.via.light.rpc.server.event;

import com.via.light.rpc.protocol.Packet;
import com.via.light.rpc.remoting.Channel;
import com.via.light.rpc.server.event.enums.ServiceEnum;

/**
 * Created by stereo on 16-8-18.
 */
public class ResponseEvent extends ServiceEvent<Packet> {
    public ResponseEvent(Packet target, Channel channel) {
        super(target, ServiceEnum.RESPONSE, channel);
    }
}
