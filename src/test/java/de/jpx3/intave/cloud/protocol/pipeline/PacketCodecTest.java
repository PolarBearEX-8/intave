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

package de.jpx3.intave.cloud.protocol.pipeline;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.jpx3.intave.cloud.Session;
import de.jpx3.intave.cloud.protocol.JsonPacket;
import de.jpx3.intave.cloud.protocol.ProtocolSpecification;
import de.jpx3.intave.cloud.protocol.listener.Clientbound;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static de.jpx3.intave.cloud.protocol.Direction.CLIENTBOUND;
import static de.jpx3.intave.cloud.protocol.Direction.SERVERBOUND;
import static org.junit.jupiter.api.Assertions.*;

final class PacketCodecTest {
  @Test
  void encodingFailureIsReportedAndDoesNotEmitPartialPacket() {
    EmbeddedChannel channel = new EmbeddedChannel(
      new PacketCodec(new ProtocolSpecification(), SERVERBOUND)
    );
    try {
      EncoderException exception = assertThrows(
        EncoderException.class,
        () -> channel.writeOutbound(new BrokenClientboundPacket())
      );

      String failure = Session.describeFailure(exception);
      assertTrue(failure.contains("BROKEN_PACKET"), failure);
      assertTrue(failure.contains("clientbound"), failure);
      assertTrue(failure.contains("version 7"), failure);
      assertTrue(failure.contains("broken payload"), failure);
      assertNull(channel.readOutbound());
    } finally {
      channel.finishAndReleaseAll();
    }
  }

  @Test
  void unknownNegotiatedPacketIdIncludesProtocolContext() {
    ProtocolSpecification protocol = new ProtocolSpecification();
    protocol.overridePacketIds(
      CLIENTBOUND,
      Arrays.asList("PLAYER_CHAT")
    );
    EmbeddedChannel channel =
      new EmbeddedChannel(new PacketCodec(protocol, CLIENTBOUND));
    ByteBuf invalidPacket = Unpooled.buffer().writeByte(99);
    try {
      DecoderException exception = assertThrows(
        DecoderException.class,
        () -> channel.writeInbound(invalidPacket)
      );

      String failure = Session.describeFailure(exception);
      assertTrue(failure.contains("clientbound"), failure);
      assertTrue(failure.contains("packet id 99"), failure);
      assertTrue(failure.contains("negotiated ids"), failure);
      assertNull(channel.readInbound());
    } finally {
      if (invalidPacket.refCnt() > 0) {
        invalidPacket.release();
      }
      channel.finishAndReleaseAll();
    }
  }

  private static final class BrokenClientboundPacket
    extends JsonPacket<Clientbound> {
    private BrokenClientboundPacket() {
      super(CLIENTBOUND, "BROKEN_PACKET", "7");
    }

    @Override
    public void serialize(JsonWriter writer) {
      throw new IllegalStateException("broken payload");
    }

    @Override
    public void deserialize(JsonReader reader) {
    }
  }
}
