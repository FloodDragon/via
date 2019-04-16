package com.via.light.rpc.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by LiuJing on 16-8-9.
 */
public abstract class AbstractService implements LifeService {

    private static Logger LOG = LoggerFactory.getLogger(AbstractService.class);

    public static class ServiceListeners {
        private final List<ServiceStateChangeListener> listeners =
            new ArrayList<ServiceStateChangeListener>();

        public synchronized void add(ServiceStateChangeListener l) {
            if (!listeners.contains(l)) {
                listeners.add(l);
            }
        }

        public synchronized boolean remove(ServiceStateChangeListener l) {
            return listeners.remove(l);
        }

        public synchronized void reset() {
            listeners.clear();
        }

        public void notifyListeners(LifeService service) {
            ServiceStateChangeListener[] callbacks;
            synchronized (this) {
                callbacks = listeners.toArray(new ServiceStateChangeListener[listeners.size()]);
            }
            for (ServiceStateChangeListener l : callbacks) {
                l.stateChanged(service);
            }
        }
    }

    private STATE state;

    private long startTime;

    private final String name;

    private final Object stateChangeLock = new Object();

    private final ServiceListeners listeners = new ServiceListeners();

    public AbstractService(String name) {
        this.name = name;
        state = STATE.NOTINITED;
    }

    @Override
    public final STATE getServiceState() {
        return state;
    }

    @Override
    public void init() {
        if (isInState(STATE.INITED)) {
            return;
        }
        synchronized (stateChangeLock) {
            if (enterState(STATE.INITED) != STATE.INITED) {
                try {
                    serviceInit();
                    if (isInState(STATE.INITED)) {
                        notifyListeners();
                    }
                } catch (Exception e) {
                    throw ServiceStateException.convert(e);
                }
            }
        }
    }

    @Override
    public void start() {
        if (isInState(STATE.STARTED)) {
            return;
        }
        synchronized (stateChangeLock) {
            if (enterState(STATE.STARTED) != STATE.STARTED) {
                try {
                    startTime = System.currentTimeMillis();
                    serviceStart();
                    if (isInState(STATE.STARTED)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Service " + getName() + " is started");
                        }
                        notifyListeners();
                    }
                } catch (Exception e) {
                    throw ServiceStateException.convert(e);
                }
            }
        }
    }

    @Override
    public void stop() {
        if (isInState(STATE.STOPPED)) {
            return;
        }
        synchronized (stateChangeLock) {
            if (enterState(STATE.STOPPED) != STATE.STOPPED) {
                try {
                    serviceStop();
                } catch (Exception e) {
                    throw ServiceStateException.convert(e);
                } finally {
                    notifyListeners();
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Ignoring re-entrant call to stop()");
                }
            }
        }
    }

    @Override
    public final void close() throws IOException {
        stop();
    }

    protected abstract void serviceInit() throws Exception;

    protected abstract void serviceStart() throws Exception;

    protected abstract void serviceStop() throws Exception;

    @Override
    public void registerServiceListener(ServiceStateChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void unregisterServiceListener(ServiceStateChangeListener l) {
        listeners.remove(l);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    private void notifyListeners() {
        try {
            listeners.notifyListeners(this);
        } catch (Throwable e) {
            LOG.warn("Exception while notifying listeners of " + this + ": " + e,
                e);
        }
    }

    private STATE enterState(STATE newState) {
        LifeService.STATE oldState = state;
        state = newState;
        if (oldState != newState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Service: " + getName() + " entered state " + getServiceState());
            }
        }
        return oldState;
    }

    @Override
    public final boolean isInState(LifeService.STATE expected) {
        return state.equals(expected);
    }

    @Override
    public String toString() {
        return "Service " + name + " in state " + state;
    }
}
