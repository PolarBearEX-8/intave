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
import de.jpx3.intave.cloud.protocol.listener.Serverbound;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.List;

public final class ServerboundUploadLogs extends BinaryPacket<Serverbound> {
  private int packetNonce;
  private Identity identity;
  private List<String> logs = new ArrayList<>();
  private Type type;

  public ServerboundUploadLogs() {
    super(Direction.SERVERBOUND, "UPLOAD_LOG", "1");
  }

  public ServerboundUploadLogs(Identity identity, int packetNonce, List<String> logs) {
    this();
    this.packetNonce = packetNonce;
    this.identity = identity;
    this.logs = logs;
    this.type = Type.PLAYER_VIOLATION;
  }

  @Override
  public void serialize(DataOutput buffer) {
    try {
      buffer.writeInt(packetNonce);
      if (identity != null) {
        buffer.writeBoolean(true);
        identity.serialize(buffer);
      } else {
        buffer.writeBoolean(false);
      }
      buffer.writeInt(logs.size());
      for (String log : logs) {
        buffer.writeUTF(log);
      }
      buffer.writeUTF(type.name());
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  @Override
  public void deserialize(DataInput buffer) {
    try {
      packetNonce = buffer.readInt();
      if (buffer.readBoolean()) {
        identity = Identity.from(buffer);
      }
      int logCount = buffer.readInt();
      for (int i = 0; i < logCount; i++) {
        logs.add(buffer.readUTF());
      }
      type = Type.valueOf(buffer.readUTF());
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public enum Type {
    SERVER,
    PLAYER_VIOLATION,
    PACKET_INSPECTION,
    INTAVE_EXCEPTION,
  }
}
