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

import java.util.Arrays;
import java.util.UUID;

import static de.jpx3.intave.cloud.protocol.Direction.CLIENTBOUND;
import static de.jpx3.intave.cloud.protocol.TransferMode.BINARY;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ClientboundConfirmAttestationsTest
  extends PacketSerializationTest {
  @Override
  protected PacketFixture fixture() {
    UUID first =
      UUID.fromString("10000000-0000-0000-0000-000000000001");
    UUID second =
      UUID.fromString("20000000-0000-0000-0000-000000000002");
    byte[] payload = binaryPayload(output -> {
      output.writeInt(2);
      writeUuid(output, first);
      writeUuid(output, second);
    });
    return fixture(
      CLIENTBOUND, "CONFIRM_ATTESTATIONS", "1", BINARY, payload,
      packet -> assertEquals(
        Arrays.asList(first, second),
        ((ClientboundConfirmAttestations) packet).requestIds()
      )
    );
  }
}
