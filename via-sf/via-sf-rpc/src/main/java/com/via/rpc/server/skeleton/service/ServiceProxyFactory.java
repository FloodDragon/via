package com.via.rpc.server.skeleton.service;

import com.via.common.compiler.support.JavassistCompiler;
import com.via.rpc.conf.annotation.*;
import com.via.rpc.server.api.IService;

/**
 * 生产Service代理
 * <p>
 * Created by liuj-ai on 2019/4/18.
 */
public final class ServiceProxyFactory {

    public static Class<?> createServiceProxyClass(Object service) throws Exception {
        Class<?> cls = service.getClass();
        com.via.rpc.conf.annotation.Service serviceAnnotation = cls.getAnnotation(com.via.rpc.conf.annotation.Service.class);
        String code = Service.ServiceProxyCodeTemplate.getServiceProxyCode(service, serviceAnnotation.interfaceClass());
        JavassistCompiler compiler = new JavassistCompiler();
        Class<?> clazz = compiler.compile(code, JavassistCompiler.class.getClassLoader());
        return clazz;
    }

    public static IService createServiceProxyInstance(Object service) throws Exception {
        Class<?> clazz = createServiceProxyClass(service);
        Object instance = clazz.newInstance();
        return (IService) instance;
    }
}