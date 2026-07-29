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

import static de.jpx3.intave.cloud.protocol.Direction.CLIENTBOUND;
import static de.jpx3.intave.cloud.protocol.TransferMode.BINARY;

final class ClientboundKeepAliveTest extends PacketSerializationTest {
  @Override
  protected PacketFixture fixture() {
    byte[] payload = binaryPayload(
      output -> output.writeLong(KEEP_ALIVE_TIME)
    );
    return fixture(
      CLIENTBOUND, "KEEP_ALIVE", "1", BINARY, payload,
      packet -> {
      }
    );
  }
}
