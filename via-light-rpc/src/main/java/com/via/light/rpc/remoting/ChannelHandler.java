package com.via.light.rpc.remoting;

import com.via.light.rpc.exc.RpcException;

public interface ChannelHandler {

    /**
     * on channel connected.
     * 
     * @param channel channel.
     */
    void connected(Channel channel) throws RpcException;

    /**
     * on channel disconnected.
     * 
     * @param channel channel.
     */
    void disconnected(Channel channel) throws RpcException;

    /**
     * on message sent.
     * 
     * @param channel channel.
     * @param message message.
     */
    void sent(Channel channel, Object message) throws RpcException;

    /**
     * on message received.
     * 
     * @param channel channel.
     * @param message message.
     */
    void received(Channel channel, Object message) throws RpcException;

    /**
     * on exception caught.
     * 
     * @param channel channel.
     * @param exception exception.
     */
    void caught(Channel channel, Throwable exception) throws RpcException;
}