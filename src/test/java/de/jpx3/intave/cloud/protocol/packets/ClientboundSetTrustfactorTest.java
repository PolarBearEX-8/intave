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

import de.jpx3.intave.access.player.trust.TrustFactor;

import static de.jpx3.intave.cloud.protocol.Direction.CLIENTBOUND;
import static de.jpx3.intave.cloud.protocol.TransferMode.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ClientboundSetTrustfactorTest extends PacketSerializationTest {
  @Override
  protected PacketFixture fixture() {
    byte[] payload = jsonPayload("{\"id\":42,\"factor\":\"YELLOW\"}");
    return fixture(
      CLIENTBOUND, "SET_TRUSTFACTOR", "1", JSON, payload,
      packet -> {
        ClientboundSetTrustfactor trustfactor =
          (ClientboundSetTrustfactor) packet;
        assertEquals(42L, trustfactor.id());
        assertEquals(TrustFactor.YELLOW, trustfactor.trustFactor());
      }
    );
  }
}
