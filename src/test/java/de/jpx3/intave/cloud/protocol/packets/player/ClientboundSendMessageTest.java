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

import de.jpx3.intave.cloud.protocol.listener.Clientbound;
import de.jpx3.intave.cloud.protocol.packets.PacketSerializationTest;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static de.jpx3.intave.cloud.protocol.Direction.CLIENTBOUND;
import static de.jpx3.intave.cloud.protocol.TransferMode.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

final class ClientboundSendMessageTest extends PacketSerializationTest {
  @Override
  protected PacketFixture fixture() {
    TextComponent first = new TextComponent("Cloud line one");
    first.setColor(ChatColor.GOLD);
    TextComponent second = new TextComponent("Cloud line two");
    second.setBold(true);
    String json = "{\"playerId\":42,\"lines\":["
      + jsonString(ComponentSerializer.toString(first))
      + ","
      + jsonString(ComponentSerializer.toString(second))
      + "]}";
    byte[] payload = jsonPayload(json);
    return fixture(
      CLIENTBOUND, "PLAYER_CHAT", "1", JSON, payload,
      packet -> {
        ClientboundSendMessage message = (ClientboundSendMessage) packet;
        assertEquals(42L, message.playerId());
        assertEquals(2, message.lines().size());
        assertEquals(
          ComponentSerializer.toString(first),
          ComponentSerializer.toString(message.lines().get(0))
        );
        assertEquals(
          ComponentSerializer.toString(second),
          ComponentSerializer.toString(message.lines().get(1))
        );
      }
    );
  }

  @Test
  void dispatchesToSendMessageListener() {
    ClientboundSendMessage message = new ClientboundSendMessage(
      42L,
      new TextComponent("Cloud message")
    );
    AtomicReference<ClientboundSendMessage> dispatched =
      new AtomicReference<>();

    message.accept(new Clientbound() {
      @Override
      public void onSendMessage(ClientboundSendMessage packet) {
        dispatched.set(packet);
      }
    });

    assertSame(message, dispatched.get());
  }
}
