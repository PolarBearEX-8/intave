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

package de.jpx3.intave.module.test.record;

import de.jpx3.intave.adapter.MinecraftVersion;
import de.jpx3.intave.adapter.MinecraftVersions;
import de.jpx3.intave.annotate.Nullable;
import de.jpx3.intave.block.cache.BlockCache;
import de.jpx3.intave.block.cache.MockFullBlockStaticPlane;
import de.jpx3.intave.block.collision.Collision;
import de.jpx3.intave.block.fluid.Fluid;
import de.jpx3.intave.block.fluid.Fluids;
import de.jpx3.intave.block.shape.BlockShape;
import de.jpx3.intave.block.variant.BlockVariant;
import de.jpx3.intave.block.variant.BlockVariantRegister;
import de.jpx3.intave.check.movement.physics.environment.Pose;
import de.jpx3.intave.codec.ByteBufStreamCodecs;
import de.jpx3.intave.codec.StreamCodec;
import de.jpx3.intave.module.test.record.action.Action;
import de.jpx3.intave.module.test.record.action.ReceiveVelocity;
import de.jpx3.intave.player.attribute.Attribute;
import de.jpx3.intave.resource.Resource;
import de.jpx3.intave.share.*;
import de.jpx3.intave.user.User;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Material;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;

public final class MovementRecording {
	private static final StreamCodec<ByteBuf, ByteBuf, Map<Material, Map<Integer, BlockShape>>> COLLISION_SHAPES_CODEC = ByteBufStreamCodecs.mapCodec(ByteBufStreamCodecs.MATERIAL, ByteBufStreamCodecs.mapCodec(ByteBufStreamCodecs.INTEGER, BlockShape.STREAM_CODEC));

	private static final StreamCodec<ByteBuf, ByteBuf, Map<Material, Map<Integer, Fluid>>> FLUIDS_CODEC = ByteBufStreamCodecs.mapCodec(ByteBufStreamCodecs.MATERIAL, ByteBufStreamCodecs.mapCodec(ByteBufStreamCodecs.INTEGER, Fluid.STREAM_CODEC));
	private static final StreamCodec<ByteBuf, ByteBuf, Map<Material, Map<Integer, BlockVariant>>> BLOCK_VARIANTS_CODEC = ByteBufStreamCodecs.mapCodec(ByteBufStreamCodecs.MATERIAL, ByteBufStreamCodecs.mapCodec(ByteBufStreamCodecs.INTEGER, BlockVariant.STREAM_CODEC));
	private static final StreamCodec<ByteBuf, ByteBuf, Map<String, Attribute>> ATTRIBUTES_CODEC = ByteBufStreamCodecs.mapCodec(ByteBufStreamCodecs.STRING, Attribute.STREAM_CODEC);
	private static final StreamCodec<ByteBuf, ByteBuf, List<Map<String, Attribute>>> FRAME_ATTRIBUTES_CODEC = ByteBufStreamCodecs.listCodecOf(ATTRIBUTES_CODEC);

	public static final StreamCodec<ByteBuf, ByteBuf, MovementRecording> STREAM_CODEC = ByteBufStreamCodecs.smartReflectionCodecBuilder(MovementRecording.class).field("internalId", ByteBufStreamCodecs.UUID).field("clientProtocolVersion", ByteBufStreamCodecs.INTEGER, () -> 47).field("serverVersion", MinecraftVersion.STREAM_CODEC, () -> MinecraftVersions.VER1_21_4).field("frames", MoveFrame.LIST_STREAM_CODEC).field("frameAttributes", FRAME_ATTRIBUTES_CODEC, LinkedList::new).field("actions", Action.LIST_STREAM_CODEC, LinkedList::new).field("collisionShapes", COLLISION_SHAPES_CODEC, HashMap::new).field("fluids", FLUIDS_CODEC, HashMap::new).field("blockVariants", BLOCK_VARIANTS_CODEC, HashMap::new).build();

	private final UUID internalId;
	private final int clientProtocolVersion;
	private final MinecraftVersion serverVersion;
	private final List<Action> actions = new LinkedList<>();
	private final List<MoveFrame> frames = new LinkedList<>();
	private final List<Map<String, Attribute>> frameAttributes = new LinkedList<>();
	private final Map<BlockPosition, MaterialVariantStore> blocks = new HashMap<>();
	private final Map<VelocityToken, VelocityInterval> velocities = new LinkedHashMap<>();
	private final Map<Material, Map<Integer, BlockShape>> collisionShapes;
	private final Map<Material, Map<Integer, Fluid>> fluids;
	private final Map<Material, Map<Integer, BlockVariant>> blockVariants;

	MovementRecording(UUID internalId, int clientProtocolVersion, MinecraftVersion serverVersion, List<MoveFrame> frames, List<Map<String, Attribute>> frameAttributes, List<Action> actions, Map<Material, Map<Integer, BlockShape>> collisionShapes, Map<Material, Map<Integer, Fluid>> fluids, Map<Material, Map<Integer, BlockVariant>> blockVariants) {
		this.internalId = Objects.requireNonNull(internalId, "internalId cannot be null");
		this.clientProtocolVersion = clientProtocolVersion;
		this.serverVersion = Objects.requireNonNull(serverVersion, "serverVersion cannot be null");
		this.frames.addAll(Objects.requireNonNull(frames, "frames cannot be null"));
		this.frameAttributes.addAll(Objects.requireNonNull(frameAttributes, "frameAttributes cannot be null"));
		this.actions.addAll(Objects.requireNonNull(actions, "actions cannot be null"));
		this.collisionShapes = Objects.requireNonNull(collisionShapes, "collisionShapes cannot be null");
		this.fluids = Objects.requireNonNull(fluids, "fluids cannot be null");
		this.blockVariants = Objects.requireNonNull(blockVariants, "blockVariants cannot be null");
	}

	private void appendFrame(MoveFrame frame) {
		frames.add(frame);
	}

	public void insertFrame(BoundingBox boundingBox, Input input, @Nullable Position position, @Nullable Rotation rotation, BlockCache blockCache, boolean gliding) {
		insertFrame(boundingBox, input, position, rotation, blockCache, Collections.emptyMap(), gliding, null);
	}

	public void insertFrame(BoundingBox boundingBox, Input input, @Nullable Position position, @Nullable Rotation rotation, BlockCache blockCache, Map<String, Attribute> attributes, boolean gliding, @Nullable Pose physicalPose) {
		Map<BlockPosition, MaterialVariantStore> dirtyBlocks = insertAndDelta(nearbyBlocks(blockCache, boundingBox, position));
		appendFrame(new MoveFrame(position, rotation, dirtyBlocks, input, gliding, physicalPose));
		frameAttributes.add(new HashMap<>(attributes));
	}

	public void insertFrame(BoundingBox boundingBox, Input input, @Nullable Position position, @Nullable Rotation rotation, BlockCache blockCache, Map<String, Attribute> attributes, boolean gliding, @Nullable Pose physicalPose, MovementFrameState frameState) {
		Map<BlockPosition, MaterialVariantStore> dirtyBlocks = insertAndDelta(nearbyBlocks(blockCache, boundingBox, position));
		appendFrame(new MoveFrame(
			position, rotation, dirtyBlocks, input, gliding, physicalPose,
			Objects.requireNonNull(frameState, "frameState")
		));
		frameAttributes.add(new HashMap<>(attributes));
	}

	public void insertAction(Action action) {
		actions.add(action);
	}

	/**
	 * Starts a velocity acknowledgement interval at the current recording tick.
	 *
	 * <p>The interval remains recording-native until the recording is detached for serialization.
	 * This lets a replay window carry an acknowledgement across a segment boundary without a
	 * second timeline in the rolling recorder.
	 */
	public synchronized VelocityToken beginVelocity(Motion motion) {
		VelocityToken token = new VelocityToken();
		velocities.put(token, new VelocityInterval(motion.copy(), ticks()));
		return token;
	}

	/**
	 * Completes a native velocity interval using the recorder's historical inclusive end tick.
	 */
	public synchronized void completeVelocity(@Nullable VelocityToken token) {
		if (token == null) {
			return;
		}
		VelocityInterval velocity = velocities.get(token);
		if (velocity != null && velocity.endExclusive == null) {
			velocity.endExclusive = ticks() + 1;
		}
	}

	/**
	 * Converts native velocity intervals into serialized PTR actions.
	 *
	 * <p>Call this only after the recording has been detached from live capture. Open intervals are
	 * clipped to the final recorded frame; completed intervals retain the existing inclusive-end
	 * behavior where that tick exists in this recording.
	 */
	public synchronized void materializeVelocities() {
		long recordingEnd = ticks();
		for (VelocityInterval velocity : velocities.values()) {
			long endExclusive = velocity.endExclusive == null ? recordingEnd : Math.min(velocity.endExclusive, recordingEnd);
			long startInclusive = Math.max(0, velocity.startInclusive);
			if (startInclusive >= endExclusive) {
				continue;
			}
			actions.add(new ReceiveVelocity(velocity.motion.copy(), TickRange.betweenExclusive(startInclusive, endExclusive)));
		}
		velocities.clear();
	}

	/**
	 * Copies and rebases native intervals when a live recording is reduced to a frame window.
	 */
	synchronized void inheritVelocities(MovementRecording source, long fromInclusive) {
		for (Map.Entry<VelocityToken, VelocityInterval> entry : source.velocities.entrySet()) {
			VelocityInterval sourceVelocity = entry.getValue();
			Long sourceEnd = sourceVelocity.endExclusive;
			if (sourceEnd != null && sourceEnd <= fromInclusive) {
				continue;
			}
			long startInclusive = Math.max(sourceVelocity.startInclusive, fromInclusive) - fromInclusive;
			Long endExclusive = sourceEnd == null ? null : sourceEnd - fromInclusive;
			velocities.put(entry.getKey(), new VelocityInterval(sourceVelocity.motion.copy(), startInclusive, endExclusive));
		}
	}

	public long ticks() {
		return frames.size();
	}

	public boolean firstPositionHasBeenSent() {
		for (MoveFrame frame : frames) {
			if (frame.moveTo() != null) {
				return true;
			}
		}
		return false;
	}

	public boolean firstRotationHasBeenSent() {
		for (MoveFrame frame : frames) {
			if (frame.rotateTo() != null) {
				return true;
			}
		}
		return false;
	}

	public void clear() {
		frames.clear();
		frameAttributes.clear();
		actions.clear();
		blocks.clear();
		velocities.clear();
		collisionShapes.clear();
		fluids.clear();
		blockVariants.clear();
	}

	void seedBlocks(Map<BlockPosition, MaterialVariantStore> blockState) {
		blocks.clear();
		blocks.putAll(blockState);
	}

	private Map<BlockPosition, MaterialVariantStore> nearbyBlocks(BlockCache blockCache, BoundingBox boundingBox, @Nullable Position position) {
		if (position == null) {
			return Collections.emptyMap();
		}

		Map<BlockPosition, MaterialVariantStore> nearbyBlocks = new HashMap<>();
		List<BlockPosition> nearbyPositions = Collision.collectRasterizedCollisions(
			boundingBox.grow(6), MutableBlockPosition::toBlockPosition,
			blockPosition -> false, Collectors.toList()
		);
		for (BlockPosition blockPosition : nearbyPositions) {
			Material type = blockCache.typeAt(blockPosition);
			int index = blockCache.variantIndexAt(blockPosition);
			MaterialVariantStore store = MaterialVariantStore.of(type, index);
			nearbyBlocks.put(blockPosition, store);
			// check if collision shapes has the block
			if (!collisionShapes.containsKey(type) || !collisionShapes.get(type).containsKey(index)) {
				BlockShape shape = blockCache.collisionShapeAt(blockPosition);
				collisionShapes.computeIfAbsent(type, k -> new HashMap<>()).put(index, shape.normalized(blockPosition));
			}
			if (!fluids.containsKey(type) || !fluids.get(type).containsKey(index)) {
				Fluid fluid = Fluids.fluidStateOf(type, index);
				fluids.computeIfAbsent(type, k -> new HashMap<>()).put(index, fluid);
			}
			if (BlockVariantRegister.isIndexed(type)) {
				recordBlockVariant(type, index, BlockVariantRegister.variantOf(type, index));
			}
		}
		return nearbyBlocks;
	}

	private Map<BlockPosition, MaterialVariantStore> insertAndDelta(Map<BlockPosition, MaterialVariantStore> nearbyBlocks) {
		Map<BlockPosition, MaterialVariantStore> delta = new HashMap<>();
		for (Map.Entry<BlockPosition, MaterialVariantStore> entry : nearbyBlocks.entrySet()) {
			BlockPosition pos = entry.getKey();
			MaterialVariantStore newStore = entry.getValue();
			MaterialVariantStore oldStore = blocks.put(pos, newStore);

			// we don't set air as initial
			if ((oldStore == null || oldStore.type() == Material.AIR) && newStore.type() == Material.AIR) {
				continue;
			}
			if (!newStore.equals(oldStore)) {
				delta.put(pos, newStore);
			}
		}
		return delta;
	}

	public UUID internalId() {
		return internalId;
	}

	public int clientProtocolVersion() {
		return clientProtocolVersion;
	}

	public MinecraftVersion serverVersion() {
		return serverVersion;
	}

	public Map<Material, Map<Integer, BlockShape>> collisionShapes() {
		return collisionShapes;
	}

	public List<Action> actions() {
		return actions;
	}

	public List<MoveFrame> frames() {
		return frames;
	}

	public Map<String, Attribute> attributesForFrame(int frame) {
		if (frame < 0 || frame >= frameAttributes.size()) {
			return Collections.emptyMap();
		}
		return new HashMap<>(frameAttributes.get(frame));
	}

	public Map<Material, Map<Integer, Fluid>> fluids() {
		return fluids;
	}

	public void recordBlockVariant(Material type, int variantIndex, BlockVariant variant) {
		blockVariants.computeIfAbsent(type, key -> new HashMap<>()).computeIfAbsent(variantIndex, key -> variant.copy());
	}

	public @Nullable BlockVariant blockVariant(Material type, int variantIndex) {
		return blockVariants.getOrDefault(type, Collections.emptyMap()).get(variantIndex);
	}

	public Map<Material, Map<Integer, BlockVariant>> blockVariants() {
		return blockVariants;
	}

	@Override
	public String toString() {
		return "MovementRecording{" + "internalId=" + internalId + ", clientProtocolVersion=" + clientProtocolVersion + ", serverVersion='" + serverVersion + '\'' + ", frames=" + frames + ", actions=" + actions + ", collisionShapes=" + collisionShapes + ", fluids=" + fluids + ", blockVariants=" + blockVariants + '}';
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		MovementRecording that = (MovementRecording) obj;
		return Objects.equals(internalId, that.internalId) && clientProtocolVersion == that.clientProtocolVersion && Objects.equals(serverVersion, that.serverVersion) && Objects.equals(frames, that.frames) && frameAttributesEqual(frameAttributes, that.frameAttributes) && Objects.equals(collisionShapes, that.collisionShapes) && Objects.equals(actions, that.actions) && Objects.equals(fluids, that.fluids) && Objects.equals(blockVariants, that.blockVariants);
	}

	@Override
	public int hashCode() {
		return Objects.hash(internalId, clientProtocolVersion, serverVersion);
	}

	private static boolean frameAttributesEqual(List<Map<String, Attribute>> first, List<Map<String, Attribute>> second) {
		int size = Math.max(first.size(), second.size());
		for (int i = 0; i < size; i++) {
			Map<String, Attribute> firstFrame = i < first.size() ? first.get(i) : Collections.emptyMap();
			Map<String, Attribute> secondFrame = i < second.size() ? second.get(i) : Collections.emptyMap();
			if (!firstFrame.equals(secondFrame)) {
				return false;
			}
		}
		return true;
	}

	public int frameCount() {
		return frames.size();
	}

	public static MovementRecording create() {
		return create(47, MinecraftVersions.VER1_21_4);
	}

	public static MovementRecording createFor(User user) {
		return create(user.protocolVersion(), MinecraftVersion.current());
	}

	public static MovementRecording create(int clientProtocolVersion, MinecraftVersion serverVersion) {
		return new MovementRecording(UUID.randomUUID(), clientProtocolVersion, serverVersion, new LinkedList<>(), new LinkedList<>(), new ArrayList<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
	}

	public static MovementRecording loadFrom(Resource resource) throws RuntimeException {
		InputStream read = new InflaterInputStream(resource.read());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int readBytes;
		try {
			while ((readBytes = read.read(buffer)) != -1) {
				baos.write(buffer, 0, readBytes);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to read movement recording from resource: " + resource, e);
		}

		ByteBuf byteBuf = Unpooled.wrappedBuffer(baos.toByteArray());
		try {
			return STREAM_CODEC.decode(byteBuf);
		} catch (Exception e) {
			throw new RuntimeException("Failed to decode movement recording from resource: " + resource, e);
		} finally {
			byteBuf.release();
		}
	}

	public static MovementRecording random() {
		MovementRecording movementRecording = MovementRecording.create();
		MockFullBlockStaticPlane blockCache = new MockFullBlockStaticPlane();
		for (int i = 0; i < 400; i++) {
			movementRecording.insertFrame(BoundingBox.empty(), Input.random(), ThreadLocalRandom.current().nextBoolean() ? Position.immutableRandom() : null, ThreadLocalRandom.current().nextBoolean() ? Rotation.zero() : null, blockCache, ThreadLocalRandom.current().nextBoolean());
		}
		return movementRecording;
	}

	public static final class VelocityToken {
		private VelocityToken() {
		}
	}

	private static final class VelocityInterval {
		private final Motion motion;
		private final long startInclusive;
		private Long endExclusive;

		private VelocityInterval(Motion motion, long startInclusive) {
			this(motion, startInclusive, null);
		}

		private VelocityInterval(Motion motion, long startInclusive, Long endExclusive) {
			this.motion = motion;
			this.startInclusive = startInclusive;
			this.endExclusive = endExclusive;
		}
	}
}
