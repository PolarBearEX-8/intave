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

package de.jpx3.intave.cloud.protocol.compress;

import com.github.luben.zstd.ZstdCompressCtx;
import com.github.luben.zstd.ZstdDecompressCtx;

public final class ZstdCompressionAlgorithm implements CompressionAlgorithm {
	private final int level;

	public ZstdCompressionAlgorithm(int level) {
		this.level = level;
	}

	@Override
	public String name() {
		return "ZSTD";
	}

	@Override
	public Encoder newEncoder() {
		return new ZstdEncoder(level);
	}

	@Override
	public Decoder newDecoder() {
		return new ZstdDecoder();
	}

	private static final class ZstdEncoder implements Encoder {
		private final ZstdCompressCtx context;

		private ZstdEncoder(int level) {
			context = new ZstdCompressCtx().setLevel(level);
		}

		@Override
		public byte[] encode(byte[] input) {
			return context.compress(input);
		}

		@Override
		public void close() {
			context.close();
		}
	}

	private static final class ZstdDecoder implements Decoder {
		private final ZstdDecompressCtx context = new ZstdDecompressCtx();

		@Override
		public byte[] decode(byte[] input, int uncompressedSize) {
			byte[] output = context.decompress(input, uncompressedSize);
			if (output.length != uncompressedSize) {
				throw new IllegalStateException(
					"Zstd decoded " + output.length + " bytes; expected " + uncompressedSize
				);
			}
			return output;
		}

		@Override
		public void close() {
			context.close();
		}
	}
}
