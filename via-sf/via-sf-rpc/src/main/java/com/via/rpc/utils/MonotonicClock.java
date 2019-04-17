package com.via.rpc.utils;


public class MonotonicClock implements Clock {

  public long getTime() {
    return Time.monotonicNow();
  }
}
