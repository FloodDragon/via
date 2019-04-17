package com.via.rpc.server.event;

import com.via.rpc.event.Event;
import com.via.rpc.remoting.Channel;
import com.via.rpc.server.event.enums.ServiceEnum;
import com.via.rpc.utils.Time;

/**
 * Created by stereo on 16-8-18.
 */
public class ServiceEvent<T> implements Event<ServiceEnum> {
    private T target;
    private long timestamp;
    private ServiceEnum type;
    private Channel channel;

    public ServiceEvent(T target, ServiceEnum type, Channel channel) {
        this.timestamp = Time.now();
        this.target = target;
        this.type = type;
        this.channel = channel;
    }

    @Override
    public ServiceEnum getType() {
        return type;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public T getTarget() {
        return target;
    }

    public Channel getChannel() {
        return channel;
    }
}
