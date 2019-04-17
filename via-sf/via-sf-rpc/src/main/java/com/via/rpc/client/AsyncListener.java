package com.via.rpc.client;

public interface AsyncListener<T> {
	void asyncReturn(T returnValue);
}
