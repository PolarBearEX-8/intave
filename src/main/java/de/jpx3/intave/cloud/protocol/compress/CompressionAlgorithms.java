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

import com.github.luben.zstd.Zstd;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.zip.Deflater;

public final class CompressionAlgorithms {
	private static final List<String> SUPPORTED_NAMES = Collections.unmodifiableList(
		Arrays.asList("ZSTD", "GZIP", "ZLIB")
	);

	private CompressionAlgorithms() {
	}

	public static List<String> supportedNames() {
		return SUPPORTED_NAMES;
	}

	public static CompressionAlgorithm initial() {
		return new ZlibCompressionAlgorithm(Deflater.DEFAULT_COMPRESSION);
	}

	public static CompressionAlgorithm fromName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Compression algorithm cannot be null");
		}
		switch (name.trim().toUpperCase(Locale.ROOT)) {
			case "ZSTD":
				return new ZstdCompressionAlgorithm(Zstd.defaultCompressionLevel());
			case "GZIP":
			case "ZLIB":
				return initial();
			default:
				throw new IllegalArgumentException("Unsupported cloud compression algorithm: " + name);
		}
	}
}
