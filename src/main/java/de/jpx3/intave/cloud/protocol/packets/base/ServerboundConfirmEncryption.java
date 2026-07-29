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
import de.jpx3.intave.cloud.protocol.Direction;
import de.jpx3.intave.cloud.protocol.listener.Serverbound;

import java.io.DataInput;
import java.io.DataOutput;
import java.nio.ByteBuffer;

public final class ServerboundConfirmEncryption extends BinaryPacket<Serverbound> {
  private ByteBuffer encryptedSharedSecret;
  private ByteBuffer encryptedVerifyToken;

  public ServerboundConfirmEncryption() {
    super(Direction.SERVERBOUND, "CONFIRM_ENCRYPTION", "1");
  }

  public ServerboundConfirmEncryption(byte[] encryptedSharedSecret, byte[] encryptedVerifyToken) {
    super(Direction.SERVERBOUND, "CONFIRM_ENCRYPTION", "1");
    this.encryptedSharedSecret = ByteBuffer.wrap(encryptedSharedSecret);
    this.encryptedVerifyToken = ByteBuffer.wrap(encryptedVerifyToken);
  }

  @Override
  public void serialize(DataOutput buffer) {
    try {
      byte[] shared = encryptedSharedSecret.array();
      buffer.writeInt(shared.length);
      buffer.write(shared);
      byte[] verify = encryptedVerifyToken.array();
      buffer.writeInt(verify.length);
      buffer.write(verify);
    } catch (Exception e) {
      throw new IllegalStateException(
        "Unable to serialize encryption confirmation packet",
        e
      );
    }
  }

  @Override
  public void deserialize(DataInput buffer) {
    try {
      int sharedLength = buffer.readInt();
      byte[] shared = new byte[sharedLength];
      buffer.readFully(shared);
      encryptedSharedSecret = ByteBuffer.wrap(shared);
      int verifyLength = buffer.readInt();
      byte[] verify = new byte[verifyLength];
      buffer.readFully(verify);
      encryptedVerifyToken = ByteBuffer.wrap(verify);
    } catch (Exception e) {
      throw new IllegalStateException(
        "Unable to deserialize encryption confirmation packet",
        e
      );
    }
  }
}
