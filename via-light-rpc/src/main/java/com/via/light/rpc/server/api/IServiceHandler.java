package com.via.light.rpc.server.api;

import com.via.light.rpc.event.Event;
import com.via.light.rpc.event.EventHandler;
import com.via.light.rpc.server.event.RequestEvent;
import com.via.light.rpc.server.event.ResponseEvent;
import com.via.light.rpc.server.event.enums.ServiceEnum;

/**
 * Created by LiuJing on 16-8-18.
 */
public interface IServiceHandler extends EventHandler<Event<ServiceEnum>> {

    //public void handleHeartbeat(HeartbeatEvent heartbeat) throws Exception;

    public void handleRequest(RequestEvent request) throws Exception;

    public void replyResponse(ResponseEvent response) throws Exception;

    public IServiceInvoker getServiceInvoker();
}
