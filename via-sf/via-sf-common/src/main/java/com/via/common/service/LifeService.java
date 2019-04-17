package com.via.common.service;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by LiuJing on 16-8-9.
 */
public interface LifeService extends Closeable {

    public enum STATE {
        NOTINITED(0, "NOTINITED"),

        INITED(1, "INITED"),

        STARTED(2, "STARTED"),

        STOPPED(3, "STOPPED");

        private final int value;

        private final String statename;

        private STATE(int value, String name) {
            this.value = value;
            this.statename = name;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return statename;
        }
    }

    void init();

    void start();

    void stop();

    void close() throws IOException;

    void registerServiceListener(ServiceStateChangeListener listener);

    void unregisterServiceListener(ServiceStateChangeListener listener);

    String getName();

    STATE getServiceState();

    long getStartTime();

    boolean isInState(STATE state);
}
