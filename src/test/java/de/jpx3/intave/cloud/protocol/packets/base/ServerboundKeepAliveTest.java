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

import de.jpx3.intave.cloud.protocol.Packet;
import de.jpx3.intave.cloud.protocol.packets.PacketSerializationTest;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.function.Consumer;

import static de.jpx3.intave.cloud.protocol.Direction.SERVERBOUND;
import static de.jpx3.intave.cloud.protocol.TransferMode.BINARY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ServerboundKeepAliveTest extends PacketSerializationTest {
  @Override
  protected PacketFixture fixture() {
    byte[] payload = binaryPayload(
      output -> output.writeLong(KEEP_ALIVE_TIME)
    );
    Consumer<Packet<?>> payloadVerifier = packet -> assertEquals(
      KEEP_ALIVE_TIME,
      ((ServerboundKeepAlive) packet).time()
    );
    Consumer<Packet<?>> codecVerifier = packet ->
      assertCurrentTime(((ServerboundKeepAlive) packet).time());
    Consumer<byte[]> outputVerifier = encoded -> {
      try {
        DataInputStream input = new DataInputStream(
          new ByteArrayInputStream(encoded)
        );
        assertCurrentTime(input.readLong());
        assertEquals(-1, input.read());
      } catch (IOException exception) {
        throw new AssertionError(exception);
      }
    };
    return fixture(
      SERVERBOUND,
      "KEEP_ALIVE",
      "1",
      BINARY,
      payload,
      payloadVerifier,
      codecVerifier,
      outputVerifier
    );
  }

  private static void assertCurrentTime(long encodedTime) {
    assertTrue(encodedTime > 0);
    assertTrue(
      Math.abs(System.currentTimeMillis() - encodedTime) < 5_000,
      "Keep-alive timestamp should be generated during encoding"
    );
  }
}
