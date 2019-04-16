package com.via.example;

import com.via.light.rpc.server.api.INotification;
import com.via.light.rpc.server.skeleton.service.Service;

/**
 * Created by liuj-ai on 2019/1/22.
 */
public class TestServiceImpl extends Service implements TestService {

    public TestServiceImpl(Class<?> cls) {
        super(cls);
    }

    @Override
    public void handleNotification(INotification notification) {

    }

    @Override
    public Entity hello(Entity s) throws Exception {
        System.out.println("hello -> " + s.getXxx());
        return s;
    }
}
