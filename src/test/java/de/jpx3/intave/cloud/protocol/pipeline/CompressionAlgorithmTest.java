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

import com.github.luben.zstd.Zstd;
import de.jpx3.intave.cloud.protocol.compress.CompressionAlgorithm;
import de.jpx3.intave.cloud.protocol.compress.ZlibCompressionAlgorithm;
import de.jpx3.intave.cloud.protocol.compress.ZstdCompressionAlgorithm;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CompressionAlgorithmTest {
	@Test
	void roundTripsZlib() {
		assertRoundTrip(new ZlibCompressionAlgorithm(Deflater.DEFAULT_COMPRESSION));
	}

	@Test
	void roundTripsZstd() {
		assertRoundTrip(new ZstdCompressionAlgorithm(Zstd.defaultCompressionLevel()));
	}

	private static void assertRoundTrip(CompressionAlgorithm algorithm) {
		byte[] phrase = "cloud compression payload with intentionally repeated JSON field names"
			.getBytes(StandardCharsets.UTF_8);
		byte[] input = new byte[phrase.length * 128];
		for (int offset = 0; offset < input.length; offset += phrase.length) {
			System.arraycopy(phrase, 0, input, offset, phrase.length);
		}

		EmbeddedChannel compressor = new EmbeddedChannel(new Compression(1, algorithm));
		EmbeddedChannel decompressor = new EmbeddedChannel(new Decompression(1, algorithm));
		try {
			assertTrue(compressor.writeOutbound(Unpooled.wrappedBuffer(input)));
			ByteBuf compressed = compressor.readOutbound();
			assertTrue(decompressor.writeInbound(compressed));
			ByteBuf decompressed = decompressor.readInbound();
			assertArrayEquals(input, ByteBufUtil.getBytes(decompressed));
			decompressed.release();
		} finally {
			compressor.finishAndReleaseAll();
			decompressor.finishAndReleaseAll();
		}
	}
}
