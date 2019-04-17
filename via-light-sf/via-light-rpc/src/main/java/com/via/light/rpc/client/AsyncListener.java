package com.via.light.rpc.client;

public interface AsyncListener<T> {
	void asyncReturn(T returnValue);
}
