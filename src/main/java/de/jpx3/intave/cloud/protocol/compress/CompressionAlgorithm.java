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

public interface CompressionAlgorithm {
	String name();

	Encoder newEncoder();

	Decoder newDecoder();

	interface Encoder extends AutoCloseable {
		byte[] encode(byte[] input);

		@Override
		void close();
	}

	interface Decoder extends AutoCloseable {
		byte[] decode(byte[] input, int uncompressedSize);

		@Override
		void close();
	}
}
