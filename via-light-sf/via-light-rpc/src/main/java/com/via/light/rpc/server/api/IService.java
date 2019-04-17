package com.via.light.rpc.server.api;

/**
 * 控制接口
 * 
 * @author LiuJing
 */
public interface IService extends INotifier {

	public void onRemove();

	public void onRegister();

	public String getServiceName();

	public IService resolveService(String actionName);

	public void handleNotification(INotification notification);

	public void setServiceContext(ISkeletonContext actionContext);
}
