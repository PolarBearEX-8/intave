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

import static de.jpx3.intave.cloud.protocol.Direction.SERVERBOUND;
import static de.jpx3.intave.cloud.protocol.TransferMode.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ServerboundPlayerLogoutTest extends PacketSerializationTest {
  @Override
  protected PacketFixture fixture() {
    byte[] payload = attestedJsonPayload("{\"id\":42}");
    return fixture(
      SERVERBOUND, "PLAYER_LOGOUT", "1", JSON, payload,
      packet -> {
        assertAttestation(packet);
        assertEquals(42L, ((ServerboundPlayerLogout) packet).id());
      }
    );
  }
}
