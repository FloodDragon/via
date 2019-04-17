
package com.via.common.event;

public interface EventHandler<T extends Event> {

  void handle(T event);

}
