package com.via.rpc.server.api;

/**
 * 
 * IServiceInvoker
 * 
 * @author stereo
 * @version 2013.12.19
 * 
 */
public interface IServiceInvoker {

	public static final String BEAN_NAME = "serviceInvoker";

	Object getService(String serviceName);

	boolean invoke(IServiceCall call);

	boolean invoke(IServiceCall call, Object service);
}
