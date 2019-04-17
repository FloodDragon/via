package com.via.light.rpc.client;

import com.via.light.rpc.conf.Config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * RPC客户端代理
 *
 * Created by LiuJing on 16-8-4.
 */
public class ClientProxy extends AbstractClient {

    private final ClassLoader loader;

    public ClientProxy() {
        this(new Config());
    }

    public ClientProxy(Config config) {
        this(config, Thread.currentThread().getContextClassLoader());
    }

    public ClientProxy(Config config, ClassLoader loader) {
        super("ClientProxy" + ":" + config.getRemoteAddress().toString(), config);
        this.loader = loader;
    }

    public <T> T create(final Class<T> api) {
        return create(api, loader);
    }

    public <T> T create(Class<T> api, ClassLoader classLoader) {
        InvocationHandler invocationHandler = new RemoteProxy(this, api);
        return (T) Proxy.newProxyInstance(classLoader, new Class[] {api}, invocationHandler);
    }
}