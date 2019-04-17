package com.via.rpc.client;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class AsyncFuture<V> implements Future<V> {
	private boolean canceled = false;
	private boolean done = false;
	private V value;
	private ReentrantLock lock = new ReentrantLock();
	private Condition notNull = lock.newCondition();
	private ArrayList<AsyncListener<V>> listenerList = new ArrayList<AsyncListener<V>>();

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		canceled = true;
		return false;
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		lock.lock();
		try {
			if (!done) {
				notNull.await();
			}
		} finally {
			lock.unlock();
		}
		return value;
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		lock.lock();
		try {
			if (!done) {
				boolean success = notNull.await(timeout, unit);
				if (!success) {
					throw new TimeoutException();
				}
			}
		} finally {
			lock.unlock();
		}
		return value;
	}

	@Override
	public boolean isCancelled() {
		return canceled;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	public void done(V value) {
		lock.lock();
		try {
			if (!done) {
				this.value = value;
				this.done = true;
				notNull.signalAll();
			}
		} finally {
			lock.unlock();
		}
		for (AsyncListener<V> listener : listenerList) {
			listener.asyncReturn(value);
		}
	}

	public synchronized void addAsyncListener(AsyncListener<V> listener) {
		listenerList.add(listener);
	}
}
