
package com.via.rpc.event;

public interface EventHandler<T extends Event> {

  void handle(T event);

}
