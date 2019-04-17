package com.via.rpc.client;

import com.via.rpc.conf.Config;
import java.lang.reflect.InvocationHandler;
import com.via.common.bytecode.Proxy;

/**
 * RPC客户端代理
 * <p>
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
        return (T) Proxy.getProxy(classLoader, new Class[]{api}).newInstance(invocationHandler);
    }
}