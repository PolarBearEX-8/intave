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

package de.jpx3.intave.cloud.protocol;

import de.jpx3.intave.cloud.protocol.listener.PacketListener;

public abstract class Packet<E extends PacketListener> implements Serializable {
  private final String name;
  private final String version;
  private final Direction direction;
  private final TransferMode mode;

  public Packet(Direction direction, String name, String version, TransferMode mode) {
    this.name = name;
    this.version = version;
    this.direction = direction;
    this.mode = mode;
  }

  public void accept(E listener) {
    listener.onSelect(this);
  }

  public final Direction direction() {
    return direction;
  }

  public final String name() {
    return name;
  }

  public final String version() {
    return version;
  }

  public final TransferMode transferMode() {
    return mode;
  }
}
