
package com.via.light.rpc.remoting;

import com.via.light.rpc.exc.RpcException;
import com.via.light.rpc.conf.Config;

import java.net.InetSocketAddress;

public interface Endpoint {

    /**
     * get config.
     * 
     * @return Config
     */
    Config getConfig();

    /**
     * get channel handler.
     * 
     * @return channel handler
     */
    ChannelHandler getChannelHandler();

    /**
     * get local address.
     * 
     * @return local address.
     */
    InetSocketAddress getLocalAddress();
    
    /**
     * send message.
     *
     * @param message
     * @throws RpcException
     */
    void send(Object message) throws RpcException;

    /**
     * send message.
     * 
     * @param message
     * @param sent 是否已发送完成
     */
    void send(Object message, boolean sent) throws RpcException;

    /**
     * is closed.
     * 
     * @return closed
     */
    boolean isClosed();
}