
package com.via.light.rpc.event;

public interface EventHandler<T extends Event> {

  void handle(T event);

}
