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

package de.jpx3.intave.cloud.protocol.packets.base;

import de.jpx3.intave.cloud.protocol.BinaryPacket;
import de.jpx3.intave.cloud.protocol.listener.Serverbound;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static de.jpx3.intave.cloud.protocol.Direction.SERVERBOUND;

public final class ServerboundKeepAlive extends BinaryPacket<Serverbound> {
  private long time;

  public ServerboundKeepAlive() {
    super(SERVERBOUND, "KEEP_ALIVE", "1");
  }

  @Override
  public void serialize(DataOutput buffer) {
    try {
      buffer.writeLong(System.currentTimeMillis());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deserialize(DataInput buffer) {
    try {
      time = buffer.readLong();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public long time() {
    return time;
  }
}
