package de.jpx3.intave.cloud;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ObjectStoreTest {
	@Test
	void assemblesChunksIntoTheWritableObjectDirectory() throws Exception {
		Path root = Files.createTempDirectory("intave-objects");
		try {
			Path runs = root.resolve("runs");
			Path objects = root.resolve("objects");
			ObjectStore store = new ObjectStore(Arrays.asList(runs, objects));
			store.initialize();
			UUID requestId = UUID.randomUUID();

			store.put(requestId, "nested/value", 0, false, bytes("hello "));
			store.put(requestId, "nested/value", 1, true, bytes("world"));

			assertEquals("hello world", new String(Files.readAllBytes(objects.resolve("nested/value")), StandardCharsets.UTF_8));
			assertEquals(Arrays.asList("nested/value"), store.list());
		} finally {
			delete(root);
		}
	}

	@Test
	void rejectsTraversalAndReservedPaths() throws Exception {
		Path root = Files.createTempDirectory("intave-objects");
		try {
			ObjectStore store = new ObjectStore(Arrays.asList(root.resolve("runs"), root.resolve("objects")));
			store.initialize();

			assertThrows(Exception.class, () -> store.existingObject("../outside"));
			assertThrows(Exception.class, () -> store.existingObject(".pending/request.part"));
		} finally {
			delete(root);
		}
	}

	@Test
	void globalObjectsOverrideRunInputs() throws Exception {
		Path root = Files.createTempDirectory("intave-objects");
		try {
			Path runs = root.resolve("runs");
			Path objects = root.resolve("objects");
			Files.createDirectories(runs);
			Files.write(runs.resolve("shared"), "run".getBytes(StandardCharsets.UTF_8));
			ObjectStore store = new ObjectStore(Arrays.asList(runs, objects));
			store.initialize();
			store.put(UUID.randomUUID(), "shared", 0, true, bytes("global"));

			assertEquals(objects.resolve("shared"), store.existingObject("shared"));
		} finally {
			delete(root);
		}
	}

	private static ByteBuffer bytes(String value) {
		return ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8));
	}

	private static void delete(Path root) throws Exception {
		if (!Files.exists(root)) {
			return;
		}
		try (Stream<Path> paths = Files.walk(root)) {
			for (Path path : paths.sorted((left, right) -> right.compareTo(left)).collect(Collectors.toList())) {
				Files.deleteIfExists(path);
			}
		}
	}
}
