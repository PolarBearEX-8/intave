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

import de.jpx3.intave.cloud.protocol.packets.PacketSerializationTest;

import static de.jpx3.intave.cloud.protocol.Direction.SERVERBOUND;
import static de.jpx3.intave.cloud.protocol.TransferMode.BINARY;

final class ServerboundConfirmEncryptionTest
  extends PacketSerializationTest {
  @Override
  protected PacketFixture fixture() {
    byte[] sharedSecret = new byte[]{1, 2, 3, 4, 5};
    byte[] verifyToken = new byte[]{9, 8, 7};
    byte[] payload = binaryPayload(output -> {
      output.writeInt(sharedSecret.length);
      output.write(sharedSecret);
      output.writeInt(verifyToken.length);
      output.write(verifyToken);
    });
    return fixture(
      SERVERBOUND, "CONFIRM_ENCRYPTION", "1", BINARY, payload,
      packet -> {
      }
    );
  }
}
