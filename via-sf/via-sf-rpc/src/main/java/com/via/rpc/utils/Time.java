package com.via.rpc.utils;

import java.util.Date;

/**
 * Created by liujing11 on 2015/5/12.
 */
public final class Time {

    public static long nowNano() {
        return System.nanoTime();
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static Date dateNow() {
        return new Date(now());
    }

    public static long monotonicNow() {
        final long NANOSECONDS_PER_MILLISECOND = 1000000;
        return System.nanoTime() / NANOSECONDS_PER_MILLISECOND;
    }
}
