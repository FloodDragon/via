package com.via.light.rpc.server.api;

import com.via.light.rpc.event.Dispatcher;
import com.via.light.rpc.remoting.Channel;

/**
 * Skeleton控制器上下文
 *
 * @author stereo
 */
public interface ISkeletonContext {

    /**
     * 注册Action观察者
     *
     * @param noteName
     * @param observer
     */
    public void registerObserver(String noteName, IObserver observer);

    /**
     * 注销Action观察者
     *
     * @param noteName
     * @param notifyContext
     */
    public void removeObserver(String noteName, Object notifyContext);

    /**
     * 广播事件
     *
     * @param note
     */
    public void notifyObservers(INotification note);

    /**
     * 注册Action
     *
     * @param service
     */
    public void registerService(IService service);

    /**
     * 检索Action
     *
     * @param serviceName
     * @return
     */
    public IService retrieveService(String serviceName);

    /**
     * 执行Action
     *
     * @param notification
     */
    public void executeService(INotification notification);

    /**
     * 注销Action
     *
     * @param serviceName
     * @return
     */
    public IService removeService(String serviceName);

    /**
     * 是否有actionName的Action
     *
     * @param serviceName
     * @return
     */
    public boolean hasService(String serviceName);

    public IServiceHandler getServiceHandler();

    public Dispatcher getDispatcher();

    public ILiveliness<Channel> getLiveliness();
}