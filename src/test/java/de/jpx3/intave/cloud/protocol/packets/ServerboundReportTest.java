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

import static de.jpx3.intave.cloud.protocol.Direction.SERVERBOUND;
import static de.jpx3.intave.cloud.protocol.TransferMode.JSON;

final class ServerboundReportTest extends PacketSerializationTest {
  @Override
  protected PacketFixture fixture() {
    byte[] payload = attestedJsonPayload(
      "{\"playerId\":42,\"report\":{\"check\":\"speed\","
        + "\"score\":0.875,\"valid\":true}}"
    );
    return fixture(
      SERVERBOUND,
      "REPORT",
      "1",
      JSON,
      payload,
      PacketSerializationTest::assertAttestation
    );
  }
}
