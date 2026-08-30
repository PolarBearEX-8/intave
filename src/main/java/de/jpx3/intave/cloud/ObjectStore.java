/*
 * Copyright 2026 Intave
 *
 * This software is licensed under the PolyForm Perimeter License 1.0.0.
 * This software may be used for any purpose, except for providing to
 * others any product that competes with the software.
 */

package de.jpx3.intave.cloud;

import ac.intave.cloud.protocol.packets.base.ServerboundSendObjectList;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ObjectStore {
	private static final long UPLOAD_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(10);
	private static final String PENDING_DIRECTORY = ".pending";

	private final List<Path> inputDirectories;
	private final Path storageDirectory;
	private final Path pendingDirectory;
	private final Map<UUID, PendingUpload> pendingUploads = new HashMap<>();

	public ObjectStore() {
		this(defaultInputs());
	}

	ObjectStore(List<Path> inputDirectories) {
		if (inputDirectories == null || inputDirectories.isEmpty()) {
			throw new IllegalArgumentException("At least one object input directory is required");
		}
		List<Path> normalized = inputDirectories.stream().map(path -> path.toAbsolutePath().normalize()).collect(Collectors.toList());
		this.inputDirectories = Collections.unmodifiableList(normalized);
		this.storageDirectory = normalized.get(normalized.size() - 1);
		this.pendingDirectory = storageDirectory.resolve(PENDING_DIRECTORY);
	}

	static List<Path> defaultInputs() {
		List<Path> paths = new ArrayList<>();
//		paths.add(Paths.get("runs"));
		String appData = System.getenv("APPDATA");
		if (appData != null && !appData.isEmpty()) {
			paths.add(Paths.get(appData, "Intave", "Objects"));
		} else {
			paths.add(Paths.get(System.getProperty("user.home"), ".intave", "objects"));
		}
		return paths;
	}

	public void initialize() throws IOException {
		Files.createDirectories(storageDirectory);
		if (Files.isSymbolicLink(storageDirectory)) {
			throw new IOException("Object storage directory must not be a symbolic link: " + storageDirectory);
		}
		if (Files.isSymbolicLink(pendingDirectory)) {
			throw new IOException("Pending object directory must not be a symbolic link: " + pendingDirectory);
		}
		clearPendingDirectory();
	}

	public List<String> list() throws IOException {
		Set<String> keys = new HashSet<>();
		for (Path inputDirectory : inputDirectories) {
			if (!Files.isDirectory(inputDirectory, LinkOption.NOFOLLOW_LINKS)) {
				continue;
			}
			try (Stream<Path> paths = Files.walk(inputDirectory)) {
				paths.filter(path -> !path.startsWith(pendingDirectory)).filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)).map(inputDirectory::relativize).map(this::keyOf).forEach(keys::add);
			}
		}
		List<String> result = new ArrayList<>(keys);
		Collections.sort(result);
		if (result.size() > ServerboundSendObjectList.MAX_OBJECT_KEYS) {
			return new ArrayList<>(result.subList(0, ServerboundSendObjectList.MAX_OBJECT_KEYS));
		}
		return result;
	}

	public Path existingObject(String key) throws IOException {
		Path relative = relativePath(key);
		for (int index = inputDirectories.size() - 1; index >= 0; index--) {
			Path inputDirectory = inputDirectories.get(index);
			Path candidate = inputDirectory.resolve(relative).normalize();
			if (!candidate.startsWith(inputDirectory)) {
				throw new IOException("Object path escaped its input directory");
			}
			ensureNoSymlinkParents(inputDirectory, candidate);
			if (Files.isRegularFile(candidate, LinkOption.NOFOLLOW_LINKS)) {
				return candidate;
			}
		}
		return null;
	}

	public synchronized void put(UUID requestId, String key, int chunkIndex, boolean lastChunk, ByteBuffer data) throws IOException {
		if (requestId == null || data == null) {
			throw new IllegalArgumentException("Object request and data must not be null");
		}
		Path relative = relativePath(key);
		PendingUpload upload = pendingUploads.get(requestId);
		try {
			if (chunkIndex == 0) {
				abort(requestId, upload);
				Path temporary = pendingDirectory.resolve(requestId + ".part");
				ensurePendingDirectory();
				OutputStream output = Files.newOutputStream(temporary, StandardOpenOption.CREATE_NEW);
				upload = new PendingUpload(relative, temporary, output);
				pendingUploads.put(requestId, upload);
			} else if (upload == null || upload.nextChunk != chunkIndex || !upload.key.equals(relative)) {
				throw new IOException("Unexpected object chunk " + chunkIndex + " for request " + requestId);
			}

			ByteBuffer readable = data.duplicate();
			byte[] bytes = new byte[readable.remaining()];
			readable.get(bytes);
			upload.output.write(bytes);
			upload.nextChunk++;
			upload.lastActivity = System.currentTimeMillis();
			if (lastChunk) {
				finish(requestId, upload);
			}
		} catch (IOException | RuntimeException exception) {
			abort(requestId, upload);
			throw exception;
		}
	}

	public synchronized void erase(String key) throws IOException {
		Path target = writableObject(key);
		if (Files.isSymbolicLink(target)) {
			throw new IOException("Object path must not be a symbolic link: " + target);
		}
		Files.deleteIfExists(target);
		removeEmptyParents(target.getParent());
	}

	public synchronized void garbageCollect() {
		long now = System.currentTimeMillis();
		for (Map.Entry<UUID, PendingUpload> entry : new ArrayList<>(pendingUploads.entrySet())) {
			if (now - entry.getValue().lastActivity >= UPLOAD_TIMEOUT_MILLIS) {
				abort(entry.getKey(), entry.getValue());
			}
		}
	}

	public synchronized void shutdown() {
		for (Map.Entry<UUID, PendingUpload> entry : new ArrayList<>(pendingUploads.entrySet())) {
			abort(entry.getKey(), entry.getValue());
		}
		try {
			clearPendingDirectory();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	private Path writableObject(String key) throws IOException {
		Path relative = relativePath(key);
		Path target = storageDirectory.resolve(relative).normalize();
		if (!target.startsWith(storageDirectory)) {
			throw new IOException("Object path escaped storage directory");
		}
		ensureNoSymlinkParents(storageDirectory, target);
		return target;
	}

	private Path relativePath(String key) throws IOException {
		if (key == null || key.isEmpty() || key.indexOf('\0') >= 0 || key.indexOf('\\') >= 0) {
			throw new IOException("Object key is not a valid relative path");
		}
		Path relative = Paths.get(key).normalize();
		String normalized = relative.toString().replace('\\', '/');
		if (relative.isAbsolute() || relative.getNameCount() == 0 || !normalized.equals(key)) {
			throw new IOException("Object key is not a normalized relative path: " + key);
		}
		if (PENDING_DIRECTORY.equals(relative.getName(0).toString())) {
			throw new IOException("Reserved object path: " + key);
		}
		for (Path part : relative) {
			if (".".equals(part.toString()) || "..".equals(part.toString())) {
				throw new IOException("Object key contains a traversal segment: " + key);
			}
		}
		return relative;
	}

	private String keyOf(Path relative) {
		return relative.toString().replace('\\', '/');
	}

	private void finish(UUID requestId, PendingUpload upload) throws IOException {
		upload.output.close();
		Path target = writableObject(keyOf(upload.key));
		Files.createDirectories(target.getParent());
		if (Files.isSymbolicLink(target)) {
			throw new IOException("Object path must not be a symbolic link: " + target);
		}
		try {
			Files.move(upload.temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException exception) {
			Files.move(upload.temporary, target, StandardCopyOption.REPLACE_EXISTING);
		}
		pendingUploads.remove(requestId);
	}

	private void abort(UUID requestId, PendingUpload upload) {
		if (upload == null) {
			return;
		}
		pendingUploads.remove(requestId);
		try {
			upload.output.close();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		try {
			Files.deleteIfExists(upload.temporary);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	private void ensureNoSymlinkParents(Path root, Path target) throws IOException {
		Path current = root;
		Path parent = target.getParent();
		if (parent == null) {
			return;
		}
		for (Path part : root.relativize(parent)) {
			current = current.resolve(part);
			if (Files.isSymbolicLink(current)) {
				throw new IOException("Object parent must not be a symbolic link: " + current);
			}
		}
	}

	private void removeEmptyParents(Path directory) throws IOException {
		while (directory != null && !directory.equals(storageDirectory) && Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
			try (Stream<Path> children = Files.list(directory)) {
				if (children.findAny().isPresent()) {
					return;
				}
			}
			Files.deleteIfExists(directory);
			directory = directory.getParent();
		}
	}

	private void clearPendingDirectory() throws IOException {
		ensurePendingDirectory();
		List<Path> paths;
		try (Stream<Path> stream = Files.walk(pendingDirectory)) {
			paths = stream.filter(path -> !path.equals(pendingDirectory)).collect(Collectors.toList());
		}
		paths.sort(Comparator.reverseOrder());
		for (Path path : paths) {
			Files.deleteIfExists(path);
		}
	}

	private void ensurePendingDirectory() throws IOException {
		if (Files.isSymbolicLink(pendingDirectory)) {
			throw new IOException("Pending object directory must not be a symbolic link: " + pendingDirectory);
		}
		Files.createDirectories(pendingDirectory);
	}

	private static final class PendingUpload {
		private final Path key;
		private final Path temporary;
		private final OutputStream output;
		private int nextChunk;
		private long lastActivity = System.currentTimeMillis();

		private PendingUpload(Path key, Path temporary, OutputStream output) {
			this.key = key;
			this.temporary = temporary;
			this.output = output;
		}
	}
}
