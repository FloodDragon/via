package com.via.light.rpc.utils;


public class MonotonicClock implements Clock {

  public long getTime() {
    return Time.monotonicNow();
  }
}
