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

package de.jpx3.intave.cloud.protocol.listener;

import de.jpx3.intave.cloud.protocol.Packet;
import de.jpx3.intave.cloud.protocol.packets.ClientboundCombatModifier;
import de.jpx3.intave.cloud.protocol.packets.ClientboundSetTrustfactor;
import de.jpx3.intave.cloud.protocol.packets.ClientboundViolation;
import de.jpx3.intave.cloud.protocol.packets.base.ClientboundConfirmAttestations;
import de.jpx3.intave.cloud.protocol.packets.base.ClientboundDisconnect;
import de.jpx3.intave.cloud.protocol.packets.base.ClientboundHello;
import de.jpx3.intave.cloud.protocol.packets.base.ClientboundKeepAlive;
import de.jpx3.intave.cloud.protocol.packets.player.ClientboundClarifyUnknownPlayerId;
import de.jpx3.intave.cloud.protocol.packets.player.ClientboundSendMessage;
import de.jpx3.intave.cloud.protocol.packets.player.ClientboundSetPlayerId;
import de.jpx3.intave.cloud.protocol.packets.sampling.ClientboundSetSamplingState;

public interface Clientbound extends PacketListener {

  @Override
  default void onSelect(Packet<?> packet) {
    if (packet instanceof ClientboundHello) {
      onClientHello((ClientboundHello) packet);
    } else if (packet instanceof ClientboundDisconnect) {
      onCloseConnection((ClientboundDisconnect) packet);
    } else if (packet instanceof ClientboundCombatModifier) {
      onCombatModifier((ClientboundCombatModifier) packet);
    } else if (packet instanceof ClientboundKeepAlive) {
      onKeepAlive((ClientboundKeepAlive) packet);
    } else if (packet instanceof ClientboundSetTrustfactor) {
      onSetTrustfactor((ClientboundSetTrustfactor) packet);
    } else if (packet instanceof ClientboundViolation) {
      onViolation((ClientboundViolation) packet);
    } else if (packet instanceof ClientboundSetSamplingState) {
      onChangeSampling((ClientboundSetSamplingState) packet);
    } else if (packet instanceof ClientboundConfirmAttestations) {
      onConfirmAttestations((ClientboundConfirmAttestations) packet);
    } else if (packet instanceof ClientboundClarifyUnknownPlayerId) {
      onClarifyUnknownPlayerId((ClientboundClarifyUnknownPlayerId) packet);
    } else if (packet instanceof ClientboundSetPlayerId) {
      onSetPlayerId((ClientboundSetPlayerId) packet);
    } else if (packet instanceof ClientboundSendMessage) {
      onSendMessage((ClientboundSendMessage) packet);
    } else {
      onUncaught(packet);
    }
  }

  default void onConfirmAttestations(ClientboundConfirmAttestations packet) {
    onUncaught(packet);
  }

  default void onClarifyUnknownPlayerId(ClientboundClarifyUnknownPlayerId packet) {
    onUncaught(packet);
  }

  default void onSetPlayerId(ClientboundSetPlayerId packet) {
    onUncaught(packet);
  }

  default void onClientHello(ClientboundHello packet) {
    onUncaught(packet);
  }

  default void onCloseConnection(ClientboundDisconnect packet) {
    onUncaught(packet);
  }

  default void onCombatModifier(ClientboundCombatModifier packet) {
    onUncaught(packet);
  }

  default void onKeepAlive(ClientboundKeepAlive packet) {
    onUncaught(packet);
  }

  default void onSetTrustfactor(ClientboundSetTrustfactor packet) {
    onUncaught(packet);
  }

  default void onViolation(ClientboundViolation packet) {
    onUncaught(packet);
  }

  default void onChangeSampling(ClientboundSetSamplingState packet) {
    onUncaught(packet);
  }

  default void onSendMessage(ClientboundSendMessage packet) {
    onUncaught(packet);
  }

  default void onUncaught(Packet<?> packet) {

  }
}
