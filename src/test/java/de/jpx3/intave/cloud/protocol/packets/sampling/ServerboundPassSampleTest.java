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

package de.jpx3.intave.cloud.protocol.packets.sampling;

import de.jpx3.intave.cloud.protocol.packets.PacketSerializationTest;

import static de.jpx3.intave.cloud.protocol.Direction.SERVERBOUND;
import static de.jpx3.intave.cloud.protocol.TransferMode.BINARY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ServerboundPassSampleTest extends PacketSerializationTest {
  @Override
  protected PacketFixture fixture() {
    byte[] sample = new byte[]{0, 1, 2, 3, 4, 5, -1};
    byte[] payload = binaryPayload(output -> {
      output.writeLong(42L);
      output.writeUTF(TRANSMISSION_ID.toString());
      output.writeLong(3L);
      output.writeInt(sample.length);
      output.write(sample);
    });
    return fixture(
      SERVERBOUND, "PASS_SAMPLE", "1", BINARY, payload,
      packet -> {
        ServerboundPassSample passSample =
          (ServerboundPassSample) packet;
        assertEquals(42L, passSample.id());
        assertEquals(TRANSMISSION_ID, passSample.transmissionId());
        assertEquals(3L, passSample.sampleSubIndex());
        assertArrayEquals(sample, passSample.data().array());
      }
    );
  }
}
