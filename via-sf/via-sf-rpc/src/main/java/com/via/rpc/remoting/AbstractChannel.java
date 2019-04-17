package com.via.rpc.remoting;

import com.via.rpc.exc.RpcException;
import com.via.rpc.conf.Config;

public abstract class AbstractChannel implements Channel, ChannelHandler {

    private final ChannelHandler handler;

    private volatile Config config;

    private volatile boolean closed;

    public AbstractChannel(Config config, ChannelHandler handler) {
        this.config = config;
        this.handler = handler;
    }

    public void send(Object message, boolean sent) throws RpcException {
        if (isClosed()) {
            throw new RpcException("Failed to send message "
                + (message == null ? "" : message.getClass().getName()) + ":" + message
                + ", cause: Channel closed. channel: " + getLocalAddress() + " -> " + getRemoteAddress());
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return handler;
    }

    @Override
    public String toString() {
        return getLocalAddress() + " -> " + getRemoteAddress();
    }

    @Override
    public void closeChannel() {
        closed = true;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public boolean isConnected() {
        return closed;
    }

    @Override
    public void send(Object message) throws RpcException {
        send(message, false);
    }

    @Override
    public void connected(Channel ch) throws RpcException {
        if (closed) {
            return;
        }
        handler.connected(ch);
    }

    @Override
    public void disconnected(Channel ch) throws RpcException {
        handler.disconnected(ch);
    }

    public void sent(Channel ch, Object msg) throws RpcException {
        if (closed) {
            return;
        }
        handler.sent(ch, msg);
    }

    public void received(Channel ch, Object msg) throws RpcException {
        if (closed) {
            return;
        }
        handler.received(ch, msg);
    }

    public void caught(Channel ch, Throwable ex) throws RpcException {
        handler.caught(ch, ex);
    }
}