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

package de.jpx3.intave.cloud.protocol.packets.player;

import de.jpx3.intave.cloud.protocol.packets.PacketSerializationTest;

import static de.jpx3.intave.cloud.protocol.Direction.CLIENTBOUND;
import static de.jpx3.intave.cloud.protocol.TransferMode.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ClientboundSetPlayerIdTest extends PacketSerializationTest {
  @Override
  protected PacketFixture fixture() {
    byte[] payload = jsonPayload(
      "{\"identity\":{\"uuid\":\"" + PLAYER_UUID
        + "\",\"name\":\"CloudPlayer\",\"netaddr\":\"192.0.2.10\"},"
        + "\"cloudid\":42}"
    );
    return fixture(
      CLIENTBOUND, "SET_PLAYER_ID", "1", JSON, payload,
      packet -> {
        ClientboundSetPlayerId setPlayerId =
          (ClientboundSetPlayerId) packet;
        assertIdentity(setPlayerId.identity());
        assertEquals(42L, setPlayerId.id());
      }
    );
  }
}
