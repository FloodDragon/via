package com.via.light.rpc.server.event;

import com.via.light.rpc.event.Event;
import com.via.light.rpc.remoting.Channel;
import com.via.light.rpc.server.event.enums.ServiceEnum;
import com.via.light.rpc.utils.Time;

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
