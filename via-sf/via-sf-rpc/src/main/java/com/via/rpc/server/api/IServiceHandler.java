package com.via.rpc.server.api;

import com.via.rpc.event.Event;
import com.via.rpc.event.EventHandler;
import com.via.rpc.server.event.RequestEvent;
import com.via.rpc.server.event.ResponseEvent;
import com.via.rpc.server.event.enums.ServiceEnum;

/**
 * Created by LiuJing on 16-8-18.
 */
public interface IServiceHandler extends EventHandler<Event<ServiceEnum>> {

    //public void handleHeartbeat(HeartbeatEvent heartbeat) throws Exception;

    public void handleRequest(RequestEvent request) throws Exception;

    public void replyResponse(ResponseEvent response) throws Exception;

    public IServiceInvoker getServiceInvoker();
}
