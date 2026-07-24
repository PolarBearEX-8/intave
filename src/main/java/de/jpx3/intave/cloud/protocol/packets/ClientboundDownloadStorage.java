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
import java.nio.ByteBuffer;
import java.security.MessageDigest;

public final class ClientboundDownloadStorage extends BinaryPacket<Clientbound> {
  private static final ThreadLocal<MessageDigest> digest =
    ThreadLocal.withInitial(() -> {
      try {
        return MessageDigest.getInstance("SHA-256");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

  private Identity id;
  private ByteBuffer data;

  public ClientboundDownloadStorage() {
    super(Direction.CLIENTBOUND, "SET_STORAGE", "1");
  }

  public ClientboundDownloadStorage(Identity id, ByteBuffer data) {
    super(Direction.CLIENTBOUND, "SET_STORAGE", "1");
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
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(array);
      buffer.write(digest.digest(), 0, 32);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void deserialize(DataInput input) {
    try {
      id = Identity.from(input);
      int size = input.readInt();
      byte[] array = new byte[size];
      input.readFully(array, 0, size);
      data = ByteBuffer.wrap(array);
      byte[] hash = new byte[32];
      input.readFully(hash, 0, 32);
      if (!MessageDigest.isEqual(hash, digest.get().digest(array))) {
        throw new RuntimeException("Hash mismatch");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Identity id() {
    return id;
  }

  public ByteBuffer data() {
    return data;
  }
}
