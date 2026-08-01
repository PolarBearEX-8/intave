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

package de.jpx3.intave.cloud;

import de.jpx3.intave.cloud.protocol.compress.CompressionAlgorithms;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class CompressionAlgorithmsTest {
	@Test
	void advertisesPreferredAndLegacyProtocolNames() {
		assertEquals(
			Arrays.asList("ZSTD", "GZIP", "ZLIB"),
			CompressionAlgorithms.supportedNames()
		);
	}

	@Test
	void resolvesProtocolNames() {
		assertEquals("ZSTD", CompressionAlgorithms.fromName("zstd").name());
		assertEquals("ZLIB", CompressionAlgorithms.fromName("gzip").name());
		assertEquals("ZLIB", CompressionAlgorithms.fromName("ZLIB").name());
	}

	@Test
	void rejectsUnsupportedProtocolName() {
		assertThrows(
			IllegalArgumentException.class,
			() -> CompressionAlgorithms.fromName("brotli")
		);
	}
}
