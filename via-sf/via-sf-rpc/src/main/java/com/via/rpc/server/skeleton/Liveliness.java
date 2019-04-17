package com.via.rpc.server.skeleton;

import com.via.common.event.Dispatcher;
import com.via.common.event.EventHandler;
import com.via.rpc.exc.RpcException;
import com.via.rpc.remoting.Channel;
import com.via.rpc.server.event.ChannelInboundEvent;
import com.via.rpc.server.listener.ClientHeartbeatBodyListener;
import com.via.rpc.server.listener.ClientLiveExpiredListener;
import com.via.rpc.protocol.Heartbeat;
import com.via.rpc.server.event.HeartbeatEvent;
import com.via.rpc.server.event.enums.HeartbeatEnum;
import com.via.rpc.server.listener.ClientRegisterListener;
import com.via.rpc.server.listener.ClientUnregisterListener;
import com.via.rpc.conf.Config;
import com.via.rpc.utils.Constants;
import com.via.rpc.utils.Time;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by LiuJing on 16-8-24.
 */
public class Liveliness extends AbstractLivelinessMonitor<Channel> implements EventHandler<HeartbeatEvent> {

    private static Logger LOG = LoggerFactory.getLogger(Liveliness.class);

    private int expireIntvl;
    private Dispatcher dispatcher;
    private ClientRegisterListener clientRegisterListener;//客户端注册心跳监听
    private ClientUnregisterListener clientUnregisterListener;//客户端注销心跳监听
    private ClientLiveExpiredListener clientLiveExpiredListener;//心跳租约超期监听
    private ClientHeartbeatBodyListener clientHeartbeatBodyListener;//心跳携带的body监控
    private ExecutorService executor = Executors.newFixedThreadPool(5);
    private Map<String, Channel> heartbeatChannelMap = new ConcurrentHashMap<>();//clientId:Channel

    public Liveliness(Config config, Dispatcher dispatcher) {
        super("Liveliness");
        this.dispatcher = dispatcher;
        this.expireIntvl = config.getHeartBeatExpireInterval();
        this.clientRegisterListener = config.getClientRegisterListener();
        this.clientUnregisterListener = config.getClientUnregisterListener();
        this.clientLiveExpiredListener = config.getClientLiveExpiredListener();
        this.clientHeartbeatBodyListener = config.getClientHeartbeatBodyListener();
    }

    public Collection<Channel> living() {
        return Collections.unmodifiableCollection(heartbeatChannelMap.values());
    }

    @Override
    protected void serviceInit() throws Exception {
        setExpireInterval(expireIntvl);
        setMonitorInterval(expireIntvl / 3);
    }

    @Override
    protected synchronized void expire(Channel channel) {
        //获取客户端Id
        String clientId = getClientId(channel);
        //删除心跳通道
        heartbeatChannelMap.remove(clientId);
        dispatcher.getEventHandler().handle(new ChannelInboundEvent(clientId, channel));
        if (clientLiveExpiredListener != null) {
            executor.execute(() -> clientLiveExpiredListener.expired(clientId, channel));
        }
    }

    protected synchronized void register(Heartbeat heartbeat, Channel channel) {
        super.register(channel);
        String clientId = heartbeat.getClient_id();
        setClientId(channel, clientId);
        heartbeatChannelMap.put(clientId, channel);
        if (clientRegisterListener != null) {
            executor.execute(() -> clientRegisterListener.registered(clientId, channel));
        }
    }

    protected synchronized void unregister(Heartbeat heartbeat) {
        String clientId = heartbeat.getClient_id();
        Channel channel = heartbeatChannelMap.remove(clientId);
        unregister(clientId, channel);
    }

    protected synchronized void unregister(String clientId, Channel channel) {
        super.unregister(channel);
        if (clientUnregisterListener != null) {
            executor.execute(() -> clientUnregisterListener.unregistered(clientId, channel));
        }
    }

    protected synchronized void heartbeat(Heartbeat heartbeat, Channel channel) {
        String clientId = heartbeat.getClient_id();
        if (running.containsKey(channel) && heartbeatChannelMap.containsKey(clientId)) {
            receivedPing(channel);
            if (heartbeat.getBody() != null) {
                executor.execute(() -> clientHeartbeatBodyListener.process(clientId, heartbeat.getBody()));
            }
        } else {
            register(heartbeat, channel);
        }
    }

    @Override
    public void handle(HeartbeatEvent event) {
        Channel channel = event.getChannel();
        HeartbeatEnum type = event.getType();
        Heartbeat heartbeat = event.getHeartbeat();
        try {
            switch (type) {
                case REGISTER:
                    register(heartbeat, channel);
                    break;
                case UNREGISTER:
                    if (heartbeat != null) {
                        unregister(heartbeat);
                        break;
                    } else {
                        String clientId = getClientId(channel);
                        if (StringUtils.isNotBlank(clientId))
                            unregister(clientId, channel);
                        else
                            LOG.error("unregister client id not found channel {}", channel);
                        return;
                    }
                case HEARTBEAT:
                    heartbeat(heartbeat, channel);
                    break;
            }
            heartbeat.setServer_time(Time.now());
            event.getChannel().send(event.getPacket(), true);
        } catch (RpcException e) {
            LOG.error("{} reply heartbeat failed", getName());
        }
    }

    private String getClientId(Channel channel) {
        return (String) channel.getAttribute(Constants.CHANNEL_CLIENT_ID);
    }

    private void setClientId(Channel channel, String clientId) {
        channel.setAttribute(Constants.CHANNEL_CLIENT_ID, clientId);
    }
}
