package com.via.rpc.server.event;

import com.via.rpc.event.Event;
import com.via.rpc.protocol.Heartbeat;
import com.via.rpc.protocol.Packet;
import com.via.rpc.remoting.Channel;
import com.via.rpc.server.event.enums.HeartbeatEnum;
import com.via.rpc.utils.Time;

/**
 * Created by stereo on 16-8-25.
 */
public class HeartbeatEvent implements Event<HeartbeatEnum> {
    private long timestamp;
    private HeartbeatEnum type;
    private Packet packet;
    private Channel channel;

    public HeartbeatEvent(HeartbeatEnum type, Channel channel, Packet packet)
    {
        this(type,channel);
        this.packet = packet;
    }

    protected HeartbeatEvent(HeartbeatEnum type, Channel channel)
    {
        this.type = type;
        this.channel = channel;
        this.timestamp = Time.now();
    }

    @Override
    public HeartbeatEnum getType() {
        return type;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public Heartbeat getHeartbeat() {
        return packet.getHeartbeat();
    }

    public Packet getPacket() {
        return packet;
    }

    public Channel getChannel() {
        return channel;
    }
}
