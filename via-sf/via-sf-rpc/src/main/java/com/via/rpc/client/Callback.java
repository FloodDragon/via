package com.via.rpc.client;

public interface Callback<T> {

	void call(T value);

	Class<?> getAcceptValueType();
}
