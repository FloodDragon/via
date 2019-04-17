package com.via.light.rpc.client;

import com.via.light.rpc.codec.MsgPackDecoder;
import com.via.light.rpc.codec.MsgPackEncoder;
import com.via.light.rpc.exc.ClientConnectException;
import com.via.light.rpc.exc.ClientTimeoutException;
import com.via.light.rpc.exc.RpcException;
import com.via.light.rpc.protocol.Packet;
import com.via.light.rpc.remoting.Channel;
import com.via.light.rpc.remoting.ChannelHandler;
import com.via.light.rpc.remoting.Client;
import com.via.light.rpc.remoting.RpcChannel;
import com.via.light.rpc.service.AbstractService;
import com.via.light.rpc.conf.Config;
import com.via.light.rpc.utils.NetUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;

import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;

/**
 * Created by LiuJing on 17-1-19.
 */
public abstract class AbstractClient extends AbstractService implements Client, ChannelHandler {

    private static Logger LOG = LoggerFactory.getLogger(AbstractClient.class);
    private String clientId;
    protected Config config;
    protected Bootstrap bootstrap;
    protected EventLoopGroup group;
    protected volatile boolean closed;
    protected volatile long serverTimestamp;
    protected HeartbeatMonitor heartbeatMonitor;
    protected volatile io.netty.channel.Channel channel;
    protected final Map<String, Callback> callbackMap = new ConcurrentHashMap<>();

    public AbstractClient(String name, Config config) {
        super(name);
        this.config = config;
        this.clientId = UUID.randomUUID().toString();
    }

    @Override
    protected void serviceInit() throws Exception {
    }

    @Override
    protected void serviceStart() throws Exception {
        doOpen();
        doConnect();
        heartbeatMonitor.start();
    }

    @Override
    protected void serviceStop() throws Exception {
        heartbeatMonitor.stop();
        doDisConnect();
        doClose();
    }

    protected void doReconnect() throws RpcException {
        doDisConnect();
        doConnect();
    }

    protected void doConnect() throws RpcException {
        try {
            ChannelFuture channelFuture = bootstrap.connect(config.getHost(), config.getPort()).syncUninterruptibly();
            boolean ret = channelFuture.awaitUninterruptibly(config.getConnectTimeout(), TimeUnit.MILLISECONDS);
            if (ret && channelFuture.isSuccess()) {
                channel = channelFuture.channel();
                closed = false;
                if (NetUtils.toAddressString((InetSocketAddress) channel.remoteAddress())
                        .equals(NetUtils.toAddressString((InetSocketAddress) channel.localAddress()))) {
                    closeChannel();
                    throw new ClientConnectException("Failed to connect " + config.getHost() + ":" + config.getPort()
                            + ". Cause by: Remote and local address are the same");
                }
            } else {
                throw new ClientTimeoutException(channelFuture.cause());
            }
        } catch (Exception ex) {
            throw new RpcException(ex);
        }
    }

    protected void doDisConnect() throws RpcException {
        closed = true;
        closeChannel();
    }

    protected void doOpen() throws RpcException {
        //心跳初始化
        heartbeatMonitor = new HeartbeatMonitor(this);
        heartbeatMonitor.init();

        final SslContext sslCtx;
        if (config.isSsl()) {
            try {
                sslCtx = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            } catch (SSLException e) {
                e.printStackTrace();
                throw new RpcException(e);
            }
        } else {
            sslCtx = null;
        }

        Class clazz;
        if (config.isUseEpoll()) {
            group = new EpollEventLoopGroup(config.getChildNioEventThreads());
            clazz = EpollSocketChannel.class;
        } else {
            group = new NioEventLoopGroup(config.getChildNioEventThreads());
            clazz = NioSocketChannel.class;
        }

        bootstrap = new Bootstrap().group(group)
                .channel(clazz)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.TCP_NODELAY, config.isTcpNoDelay())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeout())
                .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024)
                .option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024)
                .option(ChannelOption.SO_LINGER, config.getSoLinger())
                .option(ChannelOption.SO_SNDBUF, config.getSendBufferSize())
                .option(ChannelOption.SO_RCVBUF, config.getReceiveBufferSize())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc(), config.getHost(), config.getPort()));
                        }
                        p.addLast(
                                new MsgPackEncoder(),
                                new MsgPackDecoder(config.getPayload()),
                                new ClientHandler(AbstractClient.this, config)
                        );
                    }
                });
    }

    protected void doClose() throws RpcException {
        group.shutdownGracefully().syncUninterruptibly();
        releaseCallBack();
    }

    @Override
    public Channel getChannel() {
        io.netty.channel.Channel c = channel;
        if (c == null || !c.isActive())
            return null;
        return RpcChannel.getOrAddChannel(c, config, this);
    }

    @Override
    public void closeChannel() {
        if (null != channel) {
            io.netty.channel.Channel c = channel;
            Channel channel = RpcChannel.getChannel(c);
            if (channel != null) {
                channel.closeChannel();
            }
        }
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return getChannel().getRemoteAddress();
    }

    @Override
    public boolean isConnected() {
        return getChannel().isConnected();
    }

    @Override
    public boolean hasAttribute(String key) {
        return getChannel().hasAttribute(key);
    }

    @Override
    public Object getAttribute(String key) {
        return getChannel().getAttribute(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        getChannel().setAttribute(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        getChannel().removeAttribute(key);
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return this;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return getChannel().getLocalAddress();
    }

    @Override
    public void send(Object message) throws RpcException {
        getChannel().send(message);
    }

    @Override
    public void send(Object message, boolean sent) throws RpcException {
        getChannel().send(message, sent);
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    public String getClientId() {
        return clientId;
    }

    protected void releaseCallBack() {
        if (getCallbackSize() > 0)
            for (Callback callback : callbackMap.values())
                callback.call(null);
    }

    protected int getCallbackSize() {
        return callbackMap.size();
    }

    protected void setCallback(String messageId, Callback callback) {
        callbackMap.put(messageId, callback);
    }

    protected Callback removeCallBack(String messageId) {
        Callback ret = callbackMap.get(messageId);
        if (ret != null)
            callbackMap.remove(messageId);
        return ret;
    }

    protected <T extends Packet> AsyncFuture<T> sendPacket(T packet) throws RpcException {
        AsyncFuture<T> future = buildFuture(packet);
        try {
            if (!isClosed()) {
                send(packet, true);
                return future;
            } else
                throw new RpcException("client sendPacket connect closed");
        } catch (Exception ex) {
            LOG.error("client >>> send packet error " + "packet : " + packet);
            removeCallBack(packet.getId());
            throw new RpcException("client >>> send packet error " + "packet : " + packet, ex);
        }
    }

    protected <T extends Packet> AsyncFuture<T> buildFuture(final T packet) throws RpcException {
        if (packet != null && removeCallBack(packet.getId()) == null) {
            final AsyncFuture<T> future = new AsyncFuture<T>();
            Callback<T> callback = new Callback<T>() {

                @Override
                public Class<?> getAcceptValueType() {
                    return packet.getClass();
                }

                @Override
                public void call(T value) {
                    future.done(value);
                }
            };
            setCallback(packet.getId(), callback);
            return future;
        } else
            throw new RpcException("client >>> packet error : " + packet);
    }

    //event
    @Override
    public void connected(Channel channel) throws RpcException {
        LOG.info("client channel [" + channel + "] connected");
    }

    /**
     * on channel disconnected.
     *
     * @param channel channel.
     */
    @Override
    public void disconnected(Channel channel) throws RpcException {
        LOG.info("client channel [" + channel + "] disconnected");
    }

    /**
     * on message sent.
     *
     * @param channel channel.
     * @param message message.
     */
    @Override
    public void sent(Channel channel, Object message) throws RpcException {
        LOG.info("client channel [" + channel + "] sent msg >>> " + message);
    }

    /**
     * on message received.
     *
     * @param channel channel.
     * @param message message.
     */
    @Override
    public void received(Channel channel, Object message) throws RpcException {
        if (message instanceof Packet) {
            Packet packet = (Packet) message;
            Callback callback = removeCallBack(packet.getId());
            if (callback != null)
                callback.call(packet);
            else
                LOG.debug("client received packet:" + packet);
            /**
             switch (packet.getType())
             {
             case Constants.TYPE_REQUEST:
             break;
             case Constants.TYPE_RESPONSE:
             notify(packet);
             break;
             case Constants.TYPE_HEARTBEAT:
             break;
             case Constants.TYPE_HEARTBEAT_REQUEST_REGISTER:
             break;
             case Constants.TYPE_HEARTBEAT_REQUEST_UNREGISTER:
             break;
             default:
             LOG.error("ClientHandler.channelRead msg is " + msg);
             }*/
        } else
            LOG.warn("client channel received error msg is " + message);
    }

    /**
     * on exception caught.
     *
     * @param channel   channel.
     * @param exception exception.
     */
    @Override
    public void caught(Channel channel, Throwable exception) throws RpcException {
        LOG.error("caught error msg is " + exception);
        doReconnect();
    }

    @Override
    public HeartbeatPostMan getHeartbeatPostMan() {
        return heartbeatMonitor;
    }

    @Override
    public HeartBeatState getHeartBeatState() {
        return heartbeatMonitor;
    }

    @Override
    public ServerTimestamp serverTimestamp() {
        return () -> serverTimestamp;
    }
}