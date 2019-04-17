package com.via.example;

import com.via.rpc.server.api.IService;

/**
 * Created by liuj-ai on 2019/1/22.
 */
public interface TestService extends IService {
    Entity hello(Entity entity) throws Exception;
}
