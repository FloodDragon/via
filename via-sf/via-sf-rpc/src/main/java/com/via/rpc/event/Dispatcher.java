package com.via.rpc.event;

public interface Dispatcher {
	
	EventHandler getEventHandler();

	void register(Class<? extends Enum> eventType, EventHandler handler);
	
	void serviceStart() throws Exception;
	
	void serviceStop() throws Exception ;
}
