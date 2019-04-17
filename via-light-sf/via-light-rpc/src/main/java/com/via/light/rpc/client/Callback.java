package com.via.light.rpc.client;

public interface Callback<T> {

	void call(T value);

	Class<?> getAcceptValueType();
}
