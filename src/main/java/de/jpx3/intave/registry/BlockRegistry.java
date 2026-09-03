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

package de.jpx3.intave.registry;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedRegistry;
import de.jpx3.intave.share.MinecraftKey;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BlockRegistry {
	private final WrappedRegistry registry;
	private final Map<Integer, MinecraftKey> keysById;

	private BlockRegistry(WrappedRegistry registry, Map<Integer, MinecraftKey> keysById) {
		this.registry = registry;
		this.keysById = keysById;
	}

	public static BlockRegistry global() {
		return Holder.INSTANCE;
	}

	private static BlockRegistry create() {
		WrappedRegistry registry = WrappedRegistry.getRegistry(MinecraftReflection.getBlockClass());
		if (registry == null) {
			throw new IllegalStateException("Unable to access the Minecraft block registry");
		}

		Object handle = registryHandle(registry);
		if (!(handle instanceof Iterable)) {
			throw new IllegalStateException("Minecraft block registry is not iterable");
		}

		Map<Integer, MinecraftKey> keysById = new HashMap<>();
		for (Object block : (Iterable<?>) handle) {
			MinecraftKey key = keyOf(registry, block);
			if (key != null) {
				keysById.put(registry.getId(block), key);
			}
		}
		return new BlockRegistry(registry, keysById);
	}

	public MinecraftKey keyById(int id) {
		return keysById.get(id);
	}

	public MinecraftKey keyOf(Object block) {
		return keyOf(registry, block);
	}

	private static MinecraftKey keyOf(WrappedRegistry registry, Object block) {
		if (block == null) {
			return null;
		}
		return MinecraftKey.fromProtocolLib(registry.getKey(block));
	}

	public static Object registryHandle(WrappedRegistry registry) {
		for (Field field : instanceFields(WrappedRegistry.class)) {
			Object value = read(field, registry);
			if (value instanceof Iterable) {
				return value;
			}
		}
		throw new IllegalStateException("Unable to access the Minecraft block registry");
	}

	private static final class Holder {
		private static final BlockRegistry INSTANCE = create();
	}

	private static List<Field> instanceFields(Class<?> type) {
		List<Field> fields = new ArrayList<>();
		for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
			for (Field field : current.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers())) {
					fields.add(field);
				}
			}
		}
		return fields;
	}

	private static Object read(Field field, Object owner) {
		try {
			field.setAccessible(true);
			return field.get(owner);
		} catch (ReflectiveOperationException | RuntimeException ignored) {
			return null;
		}
	}
}
