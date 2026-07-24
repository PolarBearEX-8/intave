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
import de.jpx3.intave.cloud.protocol.Direction;
import de.jpx3.intave.cloud.protocol.Identity;
import de.jpx3.intave.cloud.protocol.listener.Clientbound;

import java.io.DataInput;
import java.io.DataOutput;

public final class ClientboundLogReceive extends BinaryPacket<Clientbound> {
  private int packetNonceResult;
  private Identity id;
  private String logId;

  public ClientboundLogReceive() {
    super(Direction.CLIENTBOUND, "RECEIVE_LOG", "1");
  }

  public Identity id() {
    return id;
  }

  public int packetNonceResult() {
    return packetNonceResult;
  }

  public String logId() {
    return logId;
  }

  @Override
  public void serialize(DataOutput buffer) {
    try {
      buffer.writeInt(packetNonceResult);
      if (id != null) {
        buffer.writeBoolean(true);
        id.serialize(buffer);
      } else {
        buffer.writeBoolean(false);
      }
      buffer.writeUTF(logId);
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Override
  public void deserialize(DataInput buffer) {
    try {
      packetNonceResult = buffer.readInt();
      if (buffer.readBoolean()) {
        id = Identity.from(buffer);
      }
      logId = buffer.readUTF();
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }
}
