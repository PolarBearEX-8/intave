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
import de.jpx3.intave.cloud.protocol.packets.ServerboundConfirmEncryption;
import de.jpx3.intave.cloud.protocol.packets.ServerboundHello;
import de.jpx3.intave.cloud.protocol.packets.ServerboundRequestStorage;

public interface Serverbound extends PacketListener {
  @Override
  default void onUncaught(Packet<?> packet) {
    if (packet instanceof ServerboundConfirmEncryption) {
      onConfirmEncryption((ServerboundConfirmEncryption)packet);
    } else if (packet instanceof ServerboundRequestStorage) {
      onRequestStorage((ServerboundRequestStorage)packet);
    } else if (packet instanceof ServerboundHello) {
      onHello((ServerboundHello)packet);
    }
  }

  default void onHello(ServerboundHello packet) {

  }

  default void onConfirmEncryption(ServerboundConfirmEncryption packet) {

  }

  default void onRequestStorage(ServerboundRequestStorage packet) {

  }
}
