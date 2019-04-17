package com.via.rpc.server.event;

import com.via.rpc.event.Event;
import com.via.rpc.remoting.Channel;
import com.via.rpc.server.event.enums.ChannelInboundEnum;
import com.via.rpc.utils.Time;

/**
 * Created by liuj-ai on 2019/3/28.
 */
public class ChannelInboundEvent implements Event<ChannelInboundEnum> {
    private long timestamp;
    private String clientId;
    private Channel channel;
    private ChannelInboundEnum type;

    public ChannelInboundEvent(String clientId, Channel channel) {
        this.type = ChannelInboundEnum.EXPIRE;
        this.clientId = clientId;
        this.channel = channel;
        this.timestamp = Time.now();
    }

    public String getClientId() {
        return clientId;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public ChannelInboundEnum getType() {
        return type;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "ExpireEvent{" +
                "timestamp=" + timestamp +
                ", clientId='" + clientId + '\'' +
                ", channel=" + channel +
                '}';
    }
}
