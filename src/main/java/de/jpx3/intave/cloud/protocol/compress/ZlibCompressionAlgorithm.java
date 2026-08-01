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

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class ZlibCompressionAlgorithm implements CompressionAlgorithm {
	private final int level;

	public ZlibCompressionAlgorithm(int level) {
		this.level = level;
	}

	@Override
	public String name() {
		return "ZLIB";
	}

	@Override
	public Encoder newEncoder() {
		return new ZlibEncoder(level);
	}

	@Override
	public Decoder newDecoder() {
		return new ZlibDecoder();
	}

	private static final class ZlibEncoder implements Encoder {
		private final Deflater deflater;
		private final byte[] buffer = new byte[8192];

		private ZlibEncoder(int level) {
			deflater = new Deflater(level);
		}

		@Override
		public byte[] encode(byte[] input) {
			ByteArrayOutputStream output = new ByteArrayOutputStream(input.length);
			deflater.setInput(input);
			deflater.finish();
			while (!deflater.finished()) {
				int written = deflater.deflate(buffer);
				if (written == 0) {
					throw new IllegalStateException("Zlib encoder made no progress");
				}
				output.write(buffer, 0, written);
			}
			deflater.reset();
			return output.toByteArray();
		}

		@Override
		public void close() {
			deflater.end();
		}
	}

	private static final class ZlibDecoder implements Decoder {
		private final Inflater inflater = new Inflater();

		@Override
		public byte[] decode(byte[] input, int uncompressedSize) {
			byte[] output = new byte[uncompressedSize];
			inflater.setInput(input);
			int offset = 0;
			try {
				while (!inflater.finished() && offset < output.length) {
					int written = inflater.inflate(output, offset, output.length - offset);
					if (written == 0) {
						throw new IllegalStateException("Zlib decoder made no progress");
					}
					offset += written;
				}
				if (!inflater.finished() || offset != output.length || inflater.getRemaining() != 0) {
					throw new IllegalStateException(
						"Zlib decoded " + offset + " bytes; expected " + output.length
					);
				}
				return output;
			} catch (DataFormatException exception) {
				throw new IllegalStateException("Invalid zlib payload", exception);
			} finally {
				inflater.reset();
			}
		}

		@Override
		public void close() {
			inflater.end();
		}
	}
}
