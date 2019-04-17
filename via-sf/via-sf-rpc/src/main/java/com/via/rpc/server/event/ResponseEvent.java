package com.via.rpc.server.event;

import com.via.rpc.protocol.Packet;
import com.via.rpc.remoting.Channel;
import com.via.rpc.server.event.enums.ServiceEnum;

/**
 * Created by stereo on 16-8-18.
 */
public class ResponseEvent extends ServiceEvent<Packet> {
    public ResponseEvent(Packet target, Channel channel) {
        super(target, ServiceEnum.RESPONSE, channel);
    }
}
