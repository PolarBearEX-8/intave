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
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.Objects;

public final class Decompression extends ByteToMessageDecoder {
  private final CompressionAlgorithm.Decoder decoder;
  private final int threshold;

  public Decompression(int threshold, CompressionAlgorithm algorithm) {
    this.threshold = threshold;
    this.decoder = Objects.requireNonNull(algorithm, "Compression algorithm cannot be null").newDecoder();
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (in.readableBytes() != 0) {
      int uncompressedSize = readVarInt(in);
      if (uncompressedSize == 0) {
        out.add(in.readBytes(in.readableBytes()));
      } else {
        if (uncompressedSize < threshold) {
          throw new RuntimeException("Invalid packet compression - size of " + uncompressedSize + " is below threshold of " + threshold);
        }
        if (uncompressedSize > 1024 * 1024 * 50) {
          throw new RuntimeException("Invalid packet compression - size of " + uncompressedSize + " is larger than protocol maximum of 50MB");
        }
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        out.add(Unpooled.wrappedBuffer(decoder.decode(bytes, uncompressedSize)));
      }
    }
  }

  @Override
  protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
    try {
      decoder.close();
    } finally {
      super.handlerRemoved0(ctx);
    }
  }

  private int readVarInt(ByteBuf in) {
    int i = 0;
    int bytePosition = 0;
    while (true) {
      int nextByte = in.readByte();
      i |= (nextByte & 0b1111111) << bytePosition++ * 7;
      if (bytePosition > 5) {
        throw new RuntimeException("VarInt too big");
      }
      if ((nextByte & 0b10000000) != 0b10000000) {
        break;
      }
    }
    return i;
  }
}
