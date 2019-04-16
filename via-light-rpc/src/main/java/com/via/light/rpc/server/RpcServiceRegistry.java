package com.via.light.rpc.server;

import com.via.light.rpc.server.api.IService;
import com.via.light.rpc.server.api.ISkeletonContext;

/**
 * Created by stereo on 16-8-11.
 */
public class RpcServiceRegistry {

    private ISkeletonContext skeletonContext;

    public RpcServiceRegistry(ISkeletonContext skeletonContext)
    {
        this.skeletonContext = skeletonContext;
    }

    public void registerService(IService service) {
        skeletonContext.registerService(service);
    }

    public IService retrieveService(String serviceName) {
        return skeletonContext.retrieveService(serviceName);
    }

    public IService removeService(String serviceName) {
        return skeletonContext.removeService(serviceName);
    }

    public boolean hasService(String serviceName) {
        return skeletonContext.hasService(serviceName);
    }

    public ISkeletonContext getSkeletonContext() {
        return skeletonContext;
    }
}
