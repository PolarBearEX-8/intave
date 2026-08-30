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

package de.jpx3.intave.cloud;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

final class PendingConsumer<T> {
  private static final long TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(10);

  private final Consumer<T> consumer;
  private final long expiresAt = System.currentTimeMillis() + TIMEOUT_MILLIS;

  PendingConsumer(Consumer<T> consumer) {
    this.consumer = Objects.requireNonNull(consumer, "consumer");
  }

  void accept(T value) {
    consumer.accept(value);
  }

  boolean expired(long now) {
    return now >= expiresAt;
  }
}
