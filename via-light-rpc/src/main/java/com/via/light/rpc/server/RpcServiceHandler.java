package com.via.light.rpc.server;

import com.via.light.rpc.event.Dispatcher;
import com.via.light.rpc.event.EventHandler;
import com.via.light.rpc.exc.RpcException;
import com.via.light.rpc.protocol.Packet;
import com.via.light.rpc.remoting.Channel;
import com.via.light.rpc.remoting.ChannelHandler;
import com.via.light.rpc.remoting.RpcChannel;
import com.via.light.rpc.server.api.IServiceHandler;
import com.via.light.rpc.server.event.ChannelInboundEvent;
import com.via.light.rpc.server.event.HeartbeatEvent;
import com.via.light.rpc.server.event.RequestEvent;
import com.via.light.rpc.server.event.enums.ChannelInboundEnum;
import com.via.light.rpc.server.event.enums.HeartbeatEnum;
import com.via.light.rpc.conf.Config;
import com.via.light.rpc.utils.Constants;
import com.via.light.rpc.utils.NetUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by stereo on 16-8-9.
 */
@io.netty.channel.ChannelHandler.Sharable
public class RpcServiceHandler extends ChannelInboundHandlerAdapter implements ChannelHandler, EventHandler<ChannelInboundEvent> {

    private static Logger LOG = LoggerFactory.getLogger(RpcServiceHandler.class);
    private Config config;
    private Dispatcher dispatcher;
    private IServiceHandler serviceHandler;
    private final Map<String, Channel> channels = new ConcurrentHashMap<>(); // <ip:port, channel>

    public Map<String, Channel> getChannels() {
        return channels;
    }

    public RpcServiceHandler(RpcServiceServer rpcServiceServer) {
        this.config = rpcServiceServer.getConfig();
        this.dispatcher = rpcServiceServer.getServiceContext().getDispatcher();
        this.dispatcher.register(ChannelInboundEnum.class, this);
        this.serviceHandler = rpcServiceServer.getServiceContext().getServiceHandler();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        RpcChannel channel = RpcChannel.getOrAddChannel(ctx.channel(), config, this);
        try {
            connected(channel);
        } finally {
            RpcChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        RpcChannel channel = RpcChannel.getOrAddChannel(ctx.channel(), config, this);
        try {
            disconnected(channel);
        } finally {
            RpcChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        RpcChannel channel = RpcChannel.getOrAddChannel(ctx.channel(), config, this);
        try {
            received(channel, message);
        } finally {
            RpcChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        RpcChannel channel = RpcChannel.getOrAddChannel(ctx.channel(), config, this);
        try {
            caught(channel, cause);
        } finally {
            RpcChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void connected(Channel channel) throws RpcException {
        if (channel != null) {
            channels.put(NetUtils.toAddressString(channel.getRemoteAddress()), channel);
        }
        LOG.info("RpcServiceHandler channel:{} connected ", channel);
    }

    @Override
    public void disconnected(Channel channel) throws RpcException {
        if (channel != null) {
            Channel actualChannel = channels.remove(NetUtils.toAddressString(channel.getRemoteAddress()));
            if (actualChannel != null) {
                actualChannel.closeChannel();
                dispatcher.getEventHandler().handle(new HeartbeatEvent(HeartbeatEnum.UNREGISTER, channel, new Packet()));
                LOG.info("RpcServiceHandler channel:{} disconnected ", channel);
            }
        } else
            LOG.error("RpcServiceHandler channel null");
    }

    @Override
    public void sent(Channel channel, Object message) throws RpcException {
        channel.send(message);
    }

    @Override
    public void received(Channel channel, Object message) throws RpcException {
        try {
            if (message != null && message instanceof Packet) {
                final Packet packet = (Packet) message;
                byte type = packet.getType();
                switch (type) {
                    case Constants.TYPE_REQUEST:
                        serviceHandler.handleRequest(new RequestEvent(packet, channel));
                        //dispatcher.getEventHandler().handle(new RequestEvent(packet, ctx));
                        break;
                    case Constants.TYPE_RESPONSE:
                        // 不支持
                        // dispatcher.getEventHandler().handle(new ResponseEvent(packet, ctx));
                        throw new RpcException("this operation is not supported");
                    case Constants.TYPE_HEARTBEAT_REQUEST_REGISTER:
                        dispatcher.getEventHandler().handle(new HeartbeatEvent(HeartbeatEnum.REGISTER, channel, packet));
                        break;
                    case Constants.TYPE_HEARTBEAT:
                        dispatcher.getEventHandler().handle(new HeartbeatEvent(HeartbeatEnum.HEARTBEAT, channel, packet));
                        break;
                    case Constants.TYPE_HEARTBEAT_REQUEST_UNREGISTER:
                        dispatcher.getEventHandler().handle(new HeartbeatEvent(HeartbeatEnum.UNREGISTER, channel, packet));
                        break;
                    default:
                        LOG.error("RpcServiceHandler received error message:{} ", message);
                }
            } else
                LOG.error("RpcServiceHandler.channelRead error message:{}", message);
        } catch (Exception e) {
            LOG.error("RpcServiceHandler handle packet:{} error", message, e);
        }
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RpcException {
        LOG.error("RpcServiceHandler channel:{} caught ", channel, exception);
    }

    @Override
    public void handle(ChannelInboundEvent event) {
        try {
            disconnected(event.getChannel());
            LOG.info("RpcServiceHandler handle expire {}", event);
        } catch (Exception ex) {
            LOG.error("RpcServiceHandler handle expire event error {}", event, ex);
        }
    }
}