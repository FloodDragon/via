package com.via.rpc.remoting;

import java.net.InetSocketAddress;
import java.util.Collection;

public interface Server extends Endpoint {
    
    /**
     * get channels.
     * 
     * @return channels
     */
    Collection<Channel> getChannels();

    /**
     * get channel.
     * 
     * @param remoteAddress
     * @return channel
     */
    Channel getChannel(InetSocketAddress remoteAddress);


    boolean isBound();

}