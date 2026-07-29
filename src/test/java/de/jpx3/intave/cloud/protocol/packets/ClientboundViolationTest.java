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

import static de.jpx3.intave.cloud.protocol.Direction.CLIENTBOUND;
import static de.jpx3.intave.cloud.protocol.TransferMode.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ClientboundViolationTest extends PacketSerializationTest {
  @Override
  protected PacketFixture fixture() {
    byte[] payload = jsonPayload(
      "{\"id\":42,\"check\":\"speed\",\"threshold\":\"cloud\","
        + "\"message\":\"moved too quickly\",\"details\":\"dx=1.25\","
        + "\"vl\":7}"
    );
    return fixture(
      CLIENTBOUND, "VIOLATION", "1", JSON, payload,
      packet -> {
        ClientboundViolation violation = (ClientboundViolation) packet;
        assertEquals(42L, violation.id());
        assertEquals("speed", violation.check());
        assertEquals("cloud", violation.threshold());
        assertEquals("moved too quickly", violation.message());
        assertEquals("dx=1.25", violation.details());
        assertEquals(7, violation.vl());
      }
    );
  }
}
