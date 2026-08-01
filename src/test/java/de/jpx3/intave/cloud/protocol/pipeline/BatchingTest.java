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

import de.jpx3.intave.cloud.protocol.ProtocolSpecification;
import de.jpx3.intave.cloud.protocol.compress.CompressionAlgorithm;
import de.jpx3.intave.cloud.protocol.compress.ZlibCompressionAlgorithm;
import de.jpx3.intave.cloud.protocol.packets.base.ServerboundKeepAlive;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;

import static de.jpx3.intave.cloud.protocol.Direction.CLIENTBOUND;
import static de.jpx3.intave.cloud.protocol.Direction.SERVERBOUND;
import static org.junit.jupiter.api.Assertions.*;

final class BatchingTest {
	private static final CompressionAlgorithm ZLIB = new ZlibCompressionAlgorithm(Deflater.DEFAULT_COMPRESSION);

	@Test
	void combinesPacketsBeforeCompressionAfterMaximumDelay() {
		EmbeddedChannel channel = new EmbeddedChannel(
			new Compression(1, ZLIB),
			new Batching(64 * 1024, 50, TimeUnit.MILLISECONDS)
		);
		ByteBuf first = repeatedBytes(512, 'a');
		ByteBuf second = repeatedBytes(512, 'a');
		int separatelyCompressedSize = compressedSize(repeatedBytes(512, 'a'))
			+ compressedSize(repeatedBytes(512, 'a'));

		try {
			ChannelFuture firstWrite = channel.writeOneOutbound(first);
			ChannelFuture secondWrite = channel.writeOneOutbound(second);
			channel.flushOutbound();
			assertFalse(firstWrite.isDone());
			assertFalse(secondWrite.isDone());
			assertNull(channel.readOutbound());

			channel.advanceTimeBy(49, TimeUnit.MILLISECONDS);
			channel.runScheduledPendingTasks();
			assertNull(channel.readOutbound());

			channel.advanceTimeBy(1, TimeUnit.MILLISECONDS);
			channel.runScheduledPendingTasks();
			ByteBuf compressedBatch = channel.readOutbound();
			assertNotNull(compressedBatch);
			assertTrue(compressedBatch.readableBytes() < separatelyCompressedSize);
			assertTrue(firstWrite.isSuccess());
			assertTrue(secondWrite.isSuccess());
			assertNull(channel.readOutbound());

			EmbeddedChannel decompressor = new EmbeddedChannel(new Decompression(1, ZLIB));
			try {
				assertTrue(decompressor.writeInbound(compressedBatch));
				ByteBuf decompressed = decompressor.readInbound();
				assertEquals(1024, decompressed.readableBytes());
				while (decompressed.isReadable()) {
					assertEquals('a', decompressed.readUnsignedByte());
				}
				decompressed.release();
			} finally {
				decompressor.finishAndReleaseAll();
			}
		} finally {
			channel.finishAndReleaseAll();
		}
	}

	@Test
	void combinedFrameDecodesIntoIndividualPackets() {
		ProtocolSpecification protocol = new ProtocolSpecification();
		EmbeddedChannel encoder = new EmbeddedChannel(
			new Prepender(),
			new Compression(1, ZLIB),
			new Batching(64 * 1024, 50, TimeUnit.MILLISECONDS),
			new PacketCodec(protocol, CLIENTBOUND)
		);
		EmbeddedChannel decoder = new EmbeddedChannel(
			new Accumulator(),
			new Decompression(1, ZLIB),
			new PacketCodec(protocol, SERVERBOUND)
		);

		try {
			assertFalse(encoder.writeOutbound(
				new ServerboundKeepAlive(), new ServerboundKeepAlive()
			));
			encoder.advanceTimeBy(50, TimeUnit.MILLISECONDS);
			encoder.runScheduledPendingTasks();

			ByteBuf frame = encoder.readOutbound();
			assertNotNull(frame);
			assertTrue(decoder.writeInbound(frame));
			assertInstanceOf(ServerboundKeepAlive.class, decoder.readInbound());
			assertInstanceOf(ServerboundKeepAlive.class, decoder.readInbound());
			assertNull(decoder.readInbound());
		} finally {
			encoder.finishAndReleaseAll();
			decoder.finishAndReleaseAll();
		}
	}

	@Test
	void flushesImmediatelyWhenBatchWouldExceedMaximumSize() {
		EmbeddedChannel channel = new EmbeddedChannel(
			new Batching(8, 1, TimeUnit.DAYS)
		);
		ByteBuf first = repeatedBytes(5, 1);
		ByteBuf second = repeatedBytes(5, 2);

		try {
			channel.writeOutbound(first);
			assertNull(channel.readOutbound());

			channel.writeOneOutbound(second);
			ByteBuf flushed = channel.readOutbound();
			assertEquals(5, flushed.readableBytes());
			while (flushed.isReadable()) {
				assertEquals(1, flushed.readUnsignedByte());
			}
			flushed.release();
			assertNull(channel.readOutbound());
		} finally {
			channel.finishAndReleaseAll();
		}
	}

	private static ByteBuf repeatedBytes(int length, int value) {
		ByteBuf buffer = Unpooled.buffer(length);
		for (int index = 0; index < length; index++) {
			buffer.writeByte(value);
		}
		return buffer;
	}

	private static int compressedSize(ByteBuf input) {
		EmbeddedChannel compressor = new EmbeddedChannel(new Compression(1, ZLIB));
		try {
			assertTrue(compressor.writeOutbound(input));
			ByteBuf compressed = compressor.readOutbound();
			int size = compressed.readableBytes();
			compressed.release();
			return size;
		} finally {
			compressor.finishAndReleaseAll();
		}
	}
}
