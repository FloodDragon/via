package com.via.example;

import com.via.light.rpc.client.ClientProxy;
import com.via.light.rpc.conf.Config;

/**
 * Created by liuj-ai on 2019/1/22.
 */
public class ClientTest {

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.setFailoverHandler((client) -> {
            System.out.println("ClientTest.failover ......");
        });
        final ClientProxy clientProxy = new ClientProxy(config);
        clientProxy.start();
        /** 测试心跳邮递机制
         HeartbeatPostMan heartbeatPostMan = clientProxy.getHeartbeatPostMan();
         for (int i = 0; i < 10; i++) {
         Thread.sleep(4000);
         heartbeatPostMan.deliver("hello:" + i);
         }
         */
        TestService testService = clientProxy.create(TestService.class);
        for (int i = 0; i < 100000; i++) {
            try {
                Entity entity = new Entity();
                entity.setXxx("xxxx " + i);
                entity = testService.hello(entity);
                System.out.println(entity);
                Thread.sleep(1000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        clientProxy.close();
    }
}
