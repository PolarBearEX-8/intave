/*
 * Copyright 2026 Intave
 *
 * This software is licensed under the PolyForm Perimeter License 1.0.0.
 * You may use this software for any purpose, except for providing to
 * others any product that competes with the software.
 *
 * A copy of the license is available at:
 *   https://polyformproject.org/licenses/perimeter/1.0.0/
 */

package de.jpx3.intave.cloud.request;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Request<TARGET> {
  private long lastUpdate;
  private final List<Consumer<TARGET>> subscribers = new ArrayList<>();

  public Request() {
    this.lastUpdate = System.currentTimeMillis();
  }

  public void subscribe(Consumer<TARGET> consumer) {
    subscribers.add(consumer);
    lastUpdate = System.currentTimeMillis();
  }

  public boolean publish(TARGET target) {
    subscribers.forEach(consumer -> consumer.accept(target));
    lastUpdate = System.currentTimeMillis();
    return true;
  }

  public long lastUpdate() {
    return lastUpdate;
  }
}
