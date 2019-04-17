package com.via.common.event;

import com.via.common.service.AbstractService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncDispatcher extends AbstractService implements Dispatcher {

	private static final Logger LOG = LoggerFactory
			.getLogger(AsyncDispatcher.class);

	private final BlockingQueue<Event> eventQueue;
	private volatile boolean stopped = false;

	private Thread eventHandlingThread;
	protected final Map<Class<? extends Enum>, EventHandler> eventDispatchers;
	private boolean exitOnDispatchException;
	
	
	public boolean isExitOnDispatchException() {
		return exitOnDispatchException;
	}

	public void setExitOnDispatchException(boolean exitOnDispatchException) {
		this.exitOnDispatchException = exitOnDispatchException;
	}

	public AsyncDispatcher() {
		this(new LinkedBlockingQueue<Event>());
	}

	public AsyncDispatcher(BlockingQueue<Event> eventQueue) {
		super("AsyncDispatcher");
		this.eventQueue = eventQueue;
		this.eventDispatchers = new HashMap<Class<? extends Enum>, EventHandler>();
	}

	Runnable createThread() {
		return new Runnable() {
			@Override
			public void run() {
				while (!stopped && !Thread.currentThread().isInterrupted()) {
					Event event;
					try {
						event = eventQueue.take();
					} catch (InterruptedException ie) {
						if (!stopped) {
							LOG.warn("AsyncDispatcher thread interrupted", ie);
						}
						return;
					}
					if (event != null) {
						dispatch(event);
					}
				}
			}
		};
	}


	@Override
	protected void serviceInit() throws Exception {
	}

	public void serviceStart() throws Exception {
		eventHandlingThread = new Thread(createThread());
		eventHandlingThread.setName("AsyncDispatcher event handler");
		eventHandlingThread.start();
	}

	public void serviceStop() throws Exception {
		stopped = true;
		if (eventHandlingThread != null) {
			eventHandlingThread.interrupt();
			try {
				eventHandlingThread.join();
			} catch (InterruptedException ie) {
				LOG.warn("Interrupted Exception while stopping", ie);
			}
		}

	}

	@SuppressWarnings("unchecked")
	protected void dispatch(Event event) {
		//if (LOG.isDebugEnabled()) {
		//	LOG.debug("Dispatching the event " + event.getClass().getName()
		//			+ "." + event.toString());
		//}

		Class<? extends Enum> type = event.getType().getDeclaringClass();

		try {
			EventHandler handler = eventDispatchers.get(type);
			if (handler != null) {
				handler.handle(event);
			} else {
				throw new Exception("No handler for registered for " + type);
			}
		} catch (Throwable t) {
			LOG.error("Error in dispatcher thread", t);
			if (exitOnDispatchException) {
				LOG.info("Exiting, bbye..");
				System.exit(-1);
			}
		}
	}

	public void register(Class<? extends Enum> eventType, EventHandler handler) {
		EventHandler<Event> registeredHandler = (EventHandler<Event>) eventDispatchers
				.get(eventType);
		LOG.info("Registering " + eventType + " for " + handler.getClass());
		if (registeredHandler == null) {
			eventDispatchers.put(eventType, handler);
		} else if (!(registeredHandler instanceof MultiListenerHandler)) {
			MultiListenerHandler multiHandler = new MultiListenerHandler();
			multiHandler.addHandler(registeredHandler);
			multiHandler.addHandler(handler);
			eventDispatchers.put(eventType, multiHandler);
		} else {
			MultiListenerHandler multiHandler = (MultiListenerHandler) registeredHandler;
			multiHandler.addHandler(handler);
		}
	}

	@Override
	public EventHandler getEventHandler() {
		return new GenericEventHandler();
	}

	class GenericEventHandler implements EventHandler<Event> {
		public void handle(Event event) {
			int qSize = eventQueue.size();
			if (qSize != 0 && qSize % 1000 == 0) {
				LOG.info("Size of event-queue is " + qSize);
			}
			int remCapacity = eventQueue.remainingCapacity();
			if (remCapacity < 1000) {
				LOG.warn("Very low remaining capacity in the event-queue: "
						+ remCapacity);
			}
			try {
				eventQueue.put(event);
			} catch (InterruptedException e) {
				if (!stopped) {
					LOG.warn("AsyncDispatcher thread interrupted", e);
				}
				throw new RuntimeException(e);
			}
		};
	}

	static class MultiListenerHandler implements EventHandler<Event> {
		List<EventHandler<Event>> listofHandlers;

		public MultiListenerHandler() {
			listofHandlers = new ArrayList<EventHandler<Event>>();
		}

		@Override
		public void handle(Event event) {
			for (EventHandler<Event> handler : listofHandlers) {
				handler.handle(event);
			}
		}

		void addHandler(EventHandler<Event> handler) {
			listofHandlers.add(handler);
		}
	}
}
