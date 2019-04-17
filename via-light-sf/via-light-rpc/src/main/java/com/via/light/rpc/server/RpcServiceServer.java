package com.via.light.rpc.server;

import com.via.light.rpc.codec.MsgPackDecoder;
import com.via.light.rpc.codec.MsgPackEncoder;
import com.via.light.rpc.exc.RpcException;
import com.via.light.rpc.remoting.ChannelHandler;
import com.via.light.rpc.remoting.Server;
import com.via.light.rpc.server.api.ILiveliness;
import com.via.light.rpc.server.api.ISkeletonContext;
import com.via.light.rpc.server.skeleton.SkeletonContext;
import com.via.light.rpc.service.AbstractService;
import com.via.light.rpc.service.LifeService;
import com.via.light.rpc.conf.Config;
import com.via.light.rpc.utils.NetUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by stereo on 16-8-4.
 */
public class RpcServiceServer extends AbstractService implements Server {

    private static Logger LOG = LoggerFactory.getLogger(RpcServiceServer.class);
    private Config config;
    private Channel channel;
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ISkeletonContext serviceContext;
    private RpcServiceRegistry registry;
    private volatile boolean closed;
    private RpcServiceHandler rpcServiceHandler;
    private Map<String, com.via.light.rpc.remoting.Channel> channels;

    public RpcServiceServer() {
        this(new Config());
    }

    public RpcServiceServer(Config config) {
        super("RpcServiceServer" + ":" + config.getRemoteAddress().toString());
        this.config = config;
        this.serviceContext = new SkeletonContext(config);
        this.registry = new RpcServiceRegistry(serviceContext);
    }

    @Override
    protected void serviceInit() throws Exception {
        //业务上下文
        ((LifeService) serviceContext).init();
        final SslContext sslCtx;
        if (config.isSsl()) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }
        Class clazz;
        if (config.isUseEpoll()) {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup(config.getChildNioEventThreads());
            clazz = EpollServerSocketChannel.class;
        } else {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(config.getChildNioEventThreads());
            clazz = NioServerSocketChannel.class;
        }
        rpcServiceHandler = new RpcServiceHandler(this);
        channels = rpcServiceHandler.getChannels();
        bootstrap = new ServerBootstrap()
            .group(bossGroup, workerGroup)
            .channel(clazz)
            .option(ChannelOption.SO_BACKLOG, 1024)
            .option(ChannelOption.SO_REUSEADDR, true)
            .option(ChannelOption.SO_KEEPALIVE, false)
            .option(ChannelOption.TCP_NODELAY, config.isTcpNoDelay())
            .option(ChannelOption.SO_LINGER, config.getSoLinger())
            .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
            .option(ChannelOption.SO_SNDBUF, config.getSendBufferSize())
            .option(ChannelOption.SO_RCVBUF, config.getReceiveBufferSize())
            .localAddress(config.getRemoteAddress())
            .handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    if (sslCtx != null) {
                        p.addLast(sslCtx.newHandler(ch.alloc()));
                    }
                    p.addLast("decoder", new MsgPackEncoder())
                        .addLast("encoder", new MsgPackDecoder(config.getPayload()))
                        .addLast("handler", rpcServiceHandler);
                }
            });
    }

    @Override
    protected void serviceStart() throws Exception {
        serviceInit();
        if (serviceContext != null)
            ((LifeService) serviceContext).start();
        if (bootstrap != null) {
            channel = bootstrap.bind(config.getHost(), config.getPort()).sync().channel();
        }
        closed = false;
    }

    @Override
    protected void serviceStop() throws Exception {
        if (serviceContext != null)
            ((LifeService) serviceContext).stop();
        if (bootstrap != null && channel != null && bossGroup != null && workerGroup != null) {
            channel.close().sync();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            bootstrap = null;
            channel = null;
            bossGroup = null;
            workerGroup = null;
        }
        closed = true;
    }

    public RpcServiceRegistry getIpcRegistry() {
        return registry;
    }

    public Config getConfig() {
        return config;
    }

    protected ISkeletonContext getServiceContext() {
        return serviceContext;
    }

    public ILiveliness<com.via.light.rpc.remoting.Channel> liveliness(){
        return serviceContext.getLiveliness();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return rpcServiceHandler;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return config.getRemoteAddress();
    }

    @Override
    public void send(Object message) throws RpcException {
        send(message, true);
    }

    @Override
    public void send(Object message, boolean sent) throws RpcException {
        Collection<com.via.light.rpc.remoting.Channel> channels = getChannels();
        for (com.via.light.rpc.remoting.Channel channel : channels) {
            if (channel.isConnected()) {
                channel.send(message, sent);
            }
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public Collection<com.via.light.rpc.remoting.Channel> getChannels() {
        Collection<com.via.light.rpc.remoting.Channel> chs = new HashSet<>();
        for (com.via.light.rpc.remoting.Channel channel : this.channels.values()) {
            if (channel.isConnected()) {
                chs.add(channel);
            } else {
                channels.remove(NetUtils.toAddressString(channel.getRemoteAddress()));
            }
        }
        return chs;
    }

    @Override
    public com.via.light.rpc.remoting.Channel getChannel(InetSocketAddress remoteAddress) {
        return channels.get(NetUtils.toAddressString(remoteAddress));
    }

    @Override
    public boolean isBound() {
        return channel.isActive();
    }
}
