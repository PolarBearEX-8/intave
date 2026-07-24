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

package de.jpx3.intave.cloud.protocol.packets;

import de.jpx3.intave.cloud.protocol.BinaryPacket;
import de.jpx3.intave.cloud.protocol.Identity;
import de.jpx3.intave.cloud.protocol.listener.Serverbound;

import java.io.DataInput;
import java.io.DataOutput;
import java.nio.ByteBuffer;

import static de.jpx3.intave.cloud.protocol.Direction.SERVERBOUND;

public final class ServerboundPassNayoro extends BinaryPacket<Serverbound> {
  private Identity id;
  private ByteBuffer data;

  public ServerboundPassNayoro() {
    super(SERVERBOUND, "PASS_SAMPLE", "1");
  }

  public ServerboundPassNayoro(Identity id, ByteBuffer data) {
    super(SERVERBOUND, "PASS_SAMPLE", "1");
    this.id = id;
    this.data = data;
  }

  @Override
  public void serialize(DataOutput buffer) {
    try {
      id.serialize(buffer);
      byte[] array = data.array();
      buffer.writeInt(array.length);
      buffer.write(array);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void deserialize(DataInput buffer) {
    try {
      id = Identity.from(buffer);
      int size = buffer.readInt();
      if (size > 1024 * 1024 * 50) {
        throw new RuntimeException("Too big");
      }
      byte[] array = new byte[size];
      buffer.readFully(array);
      data = ByteBuffer.wrap(array);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
