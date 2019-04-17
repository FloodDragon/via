package com.via.rpc.server.skeleton.service;

import com.via.rpc.event.Event;
import com.via.rpc.exc.RpcException;
import com.via.rpc.remoting.Channel;
import com.via.rpc.server.api.IServiceHandler;
import com.via.rpc.server.api.IServiceInvoker;
import com.via.rpc.server.api.ISkeletonContext;
import com.via.rpc.server.event.RequestEvent;
import com.via.rpc.server.event.ResponseEvent;
import com.via.rpc.server.event.enums.ServiceEnum;
import com.via.rpc.service.AbstractService;
import com.via.rpc.conf.Config;
import com.via.rpc.utils.Constants;
import com.via.rpc.utils.Daemon;
import com.via.rpc.utils.ThreadPoolUtils;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by LiuJing on 16-8-18.
 */
public class ServiceHandler extends AbstractService implements IServiceHandler {

    private static Logger LOG = LoggerFactory.getLogger(ServiceHandler.class);
    private Config config;
    private ExecutorService handlerPool;
    private final IServiceInvoker serviceInvoker;

    public ServiceHandler(ISkeletonContext skeletonContext, Config config) {
        super("ServiceHandler");
        serviceInvoker = new ServiceInvoker(skeletonContext);
        this.config = config;
    }

    void initHandlerPool() throws RpcException {
        int minPoolSize;
        int aliveTime;
        int maxPoolSize = config.getBusinessPoolSize();
        if (Constants.THREADPOOL_TYPE_FIXED.equals(config.getBusinessPoolType())) {
            minPoolSize = maxPoolSize;
            aliveTime = 0;
        } else if (Constants.THREADPOOL_TYPE_CACHED.equals(config.getBusinessPoolType())) {
            minPoolSize = 20;
            maxPoolSize = Math.max(minPoolSize, maxPoolSize);
            aliveTime = 60000;
        } else {
            throw new RpcException("HandlerPool-" + config.getBusinessPoolType());
        }
        boolean isPriority = Constants.QUEUE_TYPE_PRIORITY.equals(config.getBusinessPoolQueueType());
        BlockingQueue<Runnable> configQueue = ThreadPoolUtils.buildQueue(config.getBusinessPoolQueueSize(), isPriority);
        Daemon.DaemonFactory threadFactory = new Daemon.DaemonFactory();
        RejectedExecutionHandler handler = new RejectedExecutionHandler() {
            private int i = 1;

            @Override
            public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
                if (i++ % 7 == 0) {
                    i = 1;
                    LOG.warn("Task:{} has been reject for InvokerPool exhausted!" +
                                    " pool:{}, active:{}, queue:{}, taskcnt: {}",
                            new Object[]{
                                    runnable,
                                    executor.getPoolSize(),
                                    executor.getActiveCount(),
                                    executor.getQueue().size(),
                                    executor.getTaskCount()
                            });
                }
                throw new RejectedExecutionException("Biz thread pool of provider has bean exhausted");
            }
        };
        handlerPool = new ThreadPoolExecutor(minPoolSize, maxPoolSize,
                aliveTime, TimeUnit.MILLISECONDS,
                configQueue, threadFactory, handler);
    }

    void shutdown() {
        if (handlerPool != null && !handlerPool.isShutdown())
            handlerPool.shutdown();
    }

    @Override
    public void handleRequest(RequestEvent request) throws Exception {
        handlerPool.execute(() -> {
            ServiceContext.begin(request.getTarget(), request.getChannel());
            try {
                boolean succeed = serviceInvoker.invoke(new ServiceCall(request.getTarget()));
                if (succeed)
                    replyResponse(new ResponseEvent(request.getTarget(), request.getChannel()));
                else
                    throw new RuntimeException("handle request failed.");
            } catch (Exception ex) {
                LOG.error("service handler request handle failed, request packet:{} ", request.getTarget(), ex);
            } finally {
                ServiceContext.end();
            }
        });
    }

    @Override
    public void replyResponse(ResponseEvent response) throws Exception {
        Channel channel = response.getChannel();
        channel.send(response.getTarget(), true);
    }

    @Override
    public IServiceInvoker getServiceInvoker() {
        return serviceInvoker;
    }

    @Override
    public void handle(final Event<ServiceEnum> event) {
        ServiceEnum type = event.getType();
        LOG.info("ServiceHandler handle event:{} type:{}", event, type);
    }

    @Override
    protected void serviceInit() throws Exception {
        initHandlerPool();
    }

    @Override
    protected void serviceStart() throws Exception {
    }

    @Override
    protected void serviceStop() throws Exception {
        shutdown();
    }
}