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

public abstract class JsonPacket<LISTENER extends PacketListener> extends Packet<LISTENER> implements JsonSerializable {
  public JsonPacket(Direction direction, String name, String version) {
    super(direction, name, version, TransferMode.JSON);
  }
}
