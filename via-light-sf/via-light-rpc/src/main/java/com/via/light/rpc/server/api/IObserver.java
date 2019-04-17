package com.via.light.rpc.server.api;

public interface IObserver {

	public void setNotifyMethod(IFunction notifyMethod);

	public void setNotifyContext(Object notifyContext);

	public void notifyObserver(INotification notification);

	public boolean compareNotifyContext(Object object);
}
