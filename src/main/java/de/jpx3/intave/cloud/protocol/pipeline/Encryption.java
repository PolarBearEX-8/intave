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
import io.netty.handler.codec.MessageToByteEncoder;

import javax.crypto.Cipher;
import java.util.concurrent.atomic.LongAdder;

public final class Encryption extends MessageToByteEncoder<ByteBuf> {
  private byte[] inputBuffer = new byte[1024];
  private byte[] outputBuffer = new byte[1024];
  private final Cipher cipher;
  private final LongAdder sentBytes;

  public Encryption(Cipher cipher, LongAdder sentBytes) {
    this.cipher = cipher;
    this.sentBytes = sentBytes;
  }

  @Override
  protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf input, ByteBuf output) throws Exception {
    int i = input.readableBytes();
    sentBytes.add(i);
    if (this.inputBuffer.length < i) {
      this.inputBuffer = new byte[i];
    }
    input.readBytes(this.inputBuffer, 0, i);
    int outputSize = this.cipher.getOutputSize(i);
    if (this.outputBuffer.length < outputSize) {
      this.outputBuffer = new byte[outputSize];
    }
    output.writeBytes(this.outputBuffer, 0, this.cipher.update(this.inputBuffer, 0, i, this.outputBuffer));
  }
}
