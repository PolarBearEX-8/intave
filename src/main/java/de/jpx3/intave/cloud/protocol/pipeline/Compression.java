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

import de.jpx3.intave.cloud.protocol.compress.CompressionAlgorithm;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.Objects;

public final class Compression extends MessageToByteEncoder<ByteBuf> {
  private final CompressionAlgorithm.Encoder encoder;
  private final int threshold;

  public Compression(int threshold, CompressionAlgorithm algorithm) {
    this.threshold = threshold;
    this.encoder = Objects.requireNonNull(algorithm, "Compression algorithm cannot be null").newEncoder();
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
    int i = msg.readableBytes();
    if (i < this.threshold) {
      writeVarInt(out, 0);
      out.writeBytes(msg);
    } else {
      byte[] bytes = new byte[i];
      msg.readBytes(bytes);
      writeVarInt(out, bytes.length);
      out.writeBytes(encoder.encode(bytes));
    }
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    try {
      encoder.close();
    } finally {
      super.handlerRemoved(ctx);
    }
  }

  private void writeVarInt(ByteBuf out, int paramInt) {
    while (true) {
      if ((paramInt & 0xFFFFFF80) == 0) {
        out.writeByte(paramInt);
        return;
      }
      out.writeByte(paramInt & 0x7F | 0x80);
      paramInt >>>= 7;
    }
  }
}
