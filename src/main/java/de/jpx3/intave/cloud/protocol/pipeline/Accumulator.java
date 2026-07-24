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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public final class Accumulator extends ByteToMessageDecoder {
  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    in.markReaderIndex();
    if (!in.isReadable()) {
      in.resetReaderIndex();
      return;
    }
    // packet header
    if (in.readableBytes() < 5) {
      // reset and await full packet content
      in.resetReaderIndex();
      return;
    }
    byte markerBit = in.readByte();
    if (markerBit == -1) {
      int length = in.readInt();
      if (in.readableBytes() < length) {
        // reset and await full packet content
        in.resetReaderIndex();
        return;
      }
      // read full packet content
      out.add(in.readBytes(length));
    }
  }
}