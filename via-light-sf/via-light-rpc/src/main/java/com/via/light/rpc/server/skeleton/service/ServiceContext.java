package com.via.light.rpc.server.skeleton.service;

import com.via.light.rpc.protocol.Packet;
import com.via.light.rpc.remoting.Channel;

/**
 * Created by LiuJing on 16-8-17.
 */
public class ServiceContext {
    private Packet _request;
    private Channel _channel;
    private static final ThreadLocal<ServiceContext> _localContext = new ThreadLocal<ServiceContext>();

    protected static void begin(Packet _request, Channel _channel) {
        ServiceContext context = (ServiceContext) _localContext.get();
        if (context == null) {
            context = new ServiceContext();
            _localContext.set(context);
        }
        context._request = _request;
        context._channel = _channel;
    }

    protected static void end() {
        ServiceContext context = (ServiceContext) _localContext.get();
        if (context != null) {
            context._request = null;
            context._channel = null;
            _localContext.set(null);
        }
    }

    public static Packet getRequestPacket() {
        ServiceContext context = (ServiceContext) _localContext.get();

        if (context != null)
            return context._request;
        else
            return null;
    }

    public static Channel getChannelHandlerContext() {
        ServiceContext context = (ServiceContext) _localContext.get();
        if (context != null)
            return context._channel;
        else
            return null;
    }

    public static Channel getChannel() {
        ServiceContext context = (ServiceContext) _localContext.get();

        if (context != null)
            return context._channel;
        else
            return null;
    }
}
