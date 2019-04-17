
package com.via.common.event;


public abstract class AbstractEvent<TYPE extends Enum<TYPE>> 
    implements Event<TYPE> {

  private final TYPE type;
  private final long timestamp;

  public AbstractEvent(TYPE type) {
    this.type = type;
    timestamp = -1L;
  }

  public AbstractEvent(TYPE type, long timestamp) {
    this.type = type;
    this.timestamp = timestamp;
  }

  @Override
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public TYPE getType() {
    return type;
  }

  @Override
  public String toString() {
    return "EventType: " + getType();
  }
}
