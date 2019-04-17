package com.via.rpc.server.api;

public interface INotification {

	public String getName();

	public void setBody(Object body);

	public Object getBody();

	public void setType(String type);

	public Object getResult();

	public void setResult(Object result);

	public String getType();

	public String toString();
}
