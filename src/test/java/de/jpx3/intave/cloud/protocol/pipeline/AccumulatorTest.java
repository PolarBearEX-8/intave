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

import de.jpx3.intave.cloud.Session;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.TooLongFrameException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AccumulatorTest {
  @Test
  void rejectsInvalidFrameMarker() {
    assertInvalidFrame(
      Unpooled.buffer().writeByte(7).writeInt(0),
      CorruptedFrameException.class,
      "marker 7"
    );
  }

  @Test
  void rejectsOversizedFrameBeforeAccumulatingPayload() {
    assertInvalidFrame(
      Unpooled.buffer().writeByte(-1).writeInt(50 * 1024 * 1024 + 1),
      TooLongFrameException.class,
      "maximum"
    );
  }

  private static void assertInvalidFrame(
    ByteBuf frame,
    Class<? extends Throwable> failureType,
    String expectedMessage
  ) {
    EmbeddedChannel channel = new EmbeddedChannel(new Accumulator());
    try {
      Throwable exception = assertThrows(
        failureType,
        () -> channel.writeInbound(frame)
      );
      String failure = Session.describeFailure(exception);
      assertTrue(failure.contains(expectedMessage), failure);
    } finally {
      channel.finishAndReleaseAll();
    }
  }
}
