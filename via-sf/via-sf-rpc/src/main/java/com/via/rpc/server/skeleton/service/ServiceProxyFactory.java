package com.via.rpc.server.skeleton.service;

import com.via.rpc.conf.annotation.*;
import com.via.rpc.server.api.IService;

/**
 * 生产Service代理
 * <p>
 * Created by liuj-ai on 2019/4/18.
 */
public final class ServiceProxyFactory {

    public static IService createServiceProxy(Object service) throws Exception {
        Class<?> cls = service.getClass();
        com.via.rpc.conf.annotation.Service serviceAnnotation = cls.getAnnotation(com.via.rpc.conf.annotation.Service.class);
        Package clsPackage = cls.getPackage();
        Class<?> serviceInterfaceClasses = serviceAnnotation.interfaceClass();
        return null;
    }
}
