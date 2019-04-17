package com.via.rpc.server.api;

import java.util.Set;

/**
 * @author LiuJing
 * @version 2013.12.19
 */
public interface IServiceCall {

	public abstract String getId();

	public abstract boolean isSuccess();

	public abstract Class<?> getReturnType();

	public abstract Object getResult();

	public abstract Object getResultPacket();

	public abstract void setResult(Object result);

	public abstract String getMethodName();

	public abstract String getInterfaceName();

	public abstract Object[] getArguments();

	public abstract byte getStatus();

	public abstract Exception getException();

	public abstract void setStatus(byte status);

	public abstract Set<ICallback> getCallbacks();

	public abstract void registerCallback(ICallback callback);

	public abstract void setException(Exception exception);
}