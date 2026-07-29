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

import static de.jpx3.intave.cloud.protocol.Direction.CLIENTBOUND;
import static de.jpx3.intave.cloud.protocol.TransferMode.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ClientboundSetSamplingStateTest
  extends PacketSerializationTest {
  @Override
  protected PacketFixture fixture() {
    byte[] payload = jsonPayload(
      "{\"id\":42,\"state\":\"START\",\"transmissionId\":\""
        + TRANSMISSION_ID + "\"}"
    );
    return fixture(
      CLIENTBOUND, "SET_SAMPLING_STATE", "1", JSON, payload,
      packet -> {
        ClientboundSetSamplingState state =
          (ClientboundSetSamplingState) packet;
        assertEquals(42L, state.id());
        assertEquals(
          ClientboundSetSamplingState.SamplingState.START,
          state.newState()
        );
        assertEquals(TRANSMISSION_ID, state.transmissionId());
      }
    );
  }
}
