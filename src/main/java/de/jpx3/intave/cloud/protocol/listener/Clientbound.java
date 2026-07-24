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
import de.jpx3.intave.cloud.protocol.packets.*;

public interface Clientbound extends PacketListener {

  @Override
  default void onSelect(Packet<?> packet) {
    if (packet instanceof ClientboundHello) {
      onClientHello((ClientboundHello) packet);
    } else if (packet instanceof ClientboundDisconnect) {
      onCloseConnection((ClientboundDisconnect) packet);
    } else if (packet instanceof ClientboundCombatModifier) {
      onCombatModifier((ClientboundCombatModifier) packet);
    } else if (packet instanceof ClientboundDownloadStorage) {
      onDownloadStorage((ClientboundDownloadStorage) packet);
    } else if (packet instanceof ClientboundKeepAlive) {
      onKeepAlive((ClientboundKeepAlive) packet);
    } else if (packet instanceof ClientboundSetTrustfactor) {
      onSetTrustfactor((ClientboundSetTrustfactor) packet);
    } else if (packet instanceof ClientboundViolation) {
      onViolation((ClientboundViolation) packet);
    } else if (packet instanceof ClientboundShardsPacket) {
      onShardsPacket((ClientboundShardsPacket) packet);
    } else if (packet instanceof ClientboundSampleTransmissionAcknowledgement) {
      onSampleTransmissionAcknowledgement((ClientboundSampleTransmissionAcknowledgement) packet);
    } else if (packet instanceof ClientboundLogReceive) {
      onLogReceive((ClientboundLogReceive) packet);
    } else if (packet instanceof ClientboundCommand) {
      onCommand((ClientboundCommand) packet);
    } else if (packet instanceof ClientboundInquiryResponse) {
      onInquiryResponse((ClientboundInquiryResponse) packet);
    } else {
      onUncaught(packet);
    }
  }

  default void onSampleTransmissionAcknowledgement(ClientboundSampleTransmissionAcknowledgement packet) {
    onUncaught(packet);
  }

  default void onClientHello(ClientboundHello packet) {
    onUncaught(packet);
  }

  default void onCommand(ClientboundCommand packet) {
    onUncaught(packet);
  }

  default void onCloseConnection(ClientboundDisconnect packet) {
    onUncaught(packet);
  }

  default void onCombatModifier(ClientboundCombatModifier packet) {
    onUncaught(packet);
  }

  default void onDownloadStorage(ClientboundDownloadStorage packet) {
    onUncaught(packet);
  }

  default void onKeepAlive(ClientboundKeepAlive packet) {
    onUncaught(packet);
  }

  default void onSetTrustfactor(ClientboundSetTrustfactor packet) {
    onUncaught(packet);
  }

  default void onShardsPacket(ClientboundShardsPacket packet) {
    onUncaught(packet);
  }

  default void onViolation(ClientboundViolation packet) {
    onUncaught(packet);
  }

  default void onLogReceive(ClientboundLogReceive packet) {
    onUncaught(packet);
  }

  default void onInquiryResponse(ClientboundInquiryResponse packet) {
    onUncaught(packet);
  }

  default void onUncaught(Packet<?> packet) {

  }
}
