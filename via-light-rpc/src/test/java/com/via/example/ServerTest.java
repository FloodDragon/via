package com.via.example;

import com.via.light.rpc.server.RpcServiceServer;
import com.via.light.rpc.conf.Config;

/**
 * Created by liuj-ai on 2019/1/22.
 */
public class ServerTest {

    public static void main(String[] args) {
        Config config = new Config();
        config.setClientHeartbeatBodyListener((clientId, body) -> {
            System.out.println("==================================== ClientHeartbeatBodyListener ====================================");
            System.out.println("clientId:" + clientId + "    body:" + body);
            System.out.println("==================================== ClientHeartbeatBodyListener ====================================");
        });
        config.setClientRegisterListener((clientId, channel) -> {
            System.out.println("==================================== ClientRegisterListener ====================================");
            System.out.println("clientId:" + clientId + "    channel:" + channel);
            System.out.println("==================================== ClientRegisterListener ====================================");
        });
        config.setClientLiveExpiredListener((clientId, channel) -> {
            System.out.println("==================================== ClientLiveExpiredListener ====================================");
            System.out.println("clientId:" + clientId + "    channel:" + channel);
            System.out.println("==================================== ClientLiveExpiredListener ====================================");
        });
        config.setClientUnregisterListener((clientId, channel) -> {
            System.out.println("==================================== ClientUnregisterListener ====================================");
            System.out.println("clientId:" + clientId + "    channel:" + channel);
            System.out.println("==================================== ClientUnregisterListener ====================================");
        });
        RpcServiceServer rpcServiceServer = new RpcServiceServer(config);
        rpcServiceServer.getIpcRegistry().registerService(new TestServiceImpl(TestService.class));
        rpcServiceServer.start();
    }
}
