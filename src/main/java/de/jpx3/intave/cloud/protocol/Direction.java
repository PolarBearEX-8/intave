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

import de.jpx3.intave.cloud.protocol.listener.Clientbound;
import de.jpx3.intave.cloud.protocol.listener.PacketListener;
import de.jpx3.intave.cloud.protocol.listener.Serverbound;

public enum Direction {
  CLIENTBOUND(Clientbound.class),
  SERVERBOUND(Serverbound.class)

  ;

  private final Class<? extends PacketListener> listenerClass;

  Direction(Class<? extends PacketListener> listenerClass) {
    this.listenerClass = listenerClass;
  }

  public Class<? extends PacketListener> listenerClass() {
    return listenerClass;
  }

  public Direction opposite() {
    return this == CLIENTBOUND ? SERVERBOUND : CLIENTBOUND;
  }
}
