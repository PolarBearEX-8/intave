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

package de.jpx3.intave.benchmark;

import de.jpx3.intave.adapter.MinecraftVersion;
import de.jpx3.intave.adapter.MinecraftVersions;
import de.jpx3.intave.block.cache.MockFullBlockStaticPlane;
import de.jpx3.intave.block.fluid.FluidFlow;
import de.jpx3.intave.block.fluid.Fluids;
import de.jpx3.intave.block.shape.resolve.DrillResolver;
import de.jpx3.intave.block.shape.resolve.MockShapeResolverPipeline;
import de.jpx3.intave.check.movement.physics.config.MovementConfiguration;
import de.jpx3.intave.check.movement.physics.simulator.Simulator;
import de.jpx3.intave.check.movement.physics.simulator.Simulators;
import de.jpx3.intave.player.collider.Colliders;
import de.jpx3.intave.player.collider.complex.Collider;
import de.jpx3.intave.player.collider.simple.SimpleCollider;
import de.jpx3.intave.test.FakePlayerFactory;
import de.jpx3.intave.test.FakeWorldFactory;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserFactory;
import de.jpx3.intave.user.UserRepository;
import de.jpx3.intave.user.meta.MovementMetadata;
import de.jpx3.intave.user.meta.ProtocolMetadata;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.UUID;

public final class SimulatorMemoryBenchmark {
	private static final int DEFAULT_TICKS = 4_000_000;
	private static final int DEFAULT_WARMUP_TICKS = 10_000;
	private static final int DEFAULT_SAMPLE_INTERVAL = 10_000;
	private static volatile double sink;

	private SimulatorMemoryBenchmark() {
	}

	public static void main(String[] args) {
		BenchmarkOptions options = BenchmarkOptions.from(args);
		setupMinecraftVersion();
		warmUp(options.warmupTicks);
		forceGc();

		BenchmarkState state = BenchmarkState.create();
		forceGc();
		long usedHeapBefore = usedHeap();
		long start = System.nanoTime();
		MemoryRunResult result = runTicks(state, options.ticks, options.sampleInterval);
		long elapsedNanos = System.nanoTime() - start;
		long usedHeapAfter = usedHeap();
		forceGc();
		long usedHeapAfterGc = usedHeap();
		sink = result.checksum();

		long retainedDelta = usedHeapAfterGc - usedHeapBefore;
		long peakDelta = result.peakUsedHeap() - usedHeapBefore;
		System.out.println("Simulator memory benchmark");
		System.out.println("  ticks: " + options.ticks);
		System.out.println("  warmup ticks: " + options.warmupTicks);
		System.out.println("  sample interval: " + options.sampleInterval);
		System.out.println("  samples: " + result.samples());
		System.out.println("  elapsed: " + formatMillis(elapsedNanos) + " ms");
		System.out.println("  used heap before: " + formatBytes(usedHeapBefore));
		System.out.println("  used heap peak: " + formatBytes(result.peakUsedHeap()));
		System.out.println("  used heap after: " + formatBytes(usedHeapAfter));
		System.out.println("  used heap after gc: " + formatBytes(usedHeapAfterGc));
		System.out.println("  retained delta after gc: " + formatSignedBytes(retainedDelta));
		System.out.println("  peak delta: " + formatSignedBytes(peakDelta));
		System.out.println("  retained bytes/tick: " + format(retainedDelta / (double) options.ticks));
		System.out.println("  checksum: " + format(sink));
	}

	private static void setupMinecraftVersion() {
		MinecraftVersion.setCurrent(MinecraftVersions.VER1_21_4);
		com.comphenix.protocol.utility.MinecraftVersion.setCurrentVersion(
			com.comphenix.protocol.utility.MinecraftVersion.v1_21_4
		);
		DrillResolver.manualInit(MockShapeResolverPipeline.createStoneDefault());
	}

	private static void warmUp(int ticks) {
		runTicks(BenchmarkState.create(), ticks, Integer.MAX_VALUE);
	}

	private static MemoryRunResult runTicks(
		BenchmarkState state,
		int ticks,
		int sampleInterval
	) {
		double checksum = 0.0;
		long peakUsedHeap = usedHeap();
		int samples = 0;
		MovementConfiguration[] configurations = state.configurations;
		for (int tick = 0; tick < ticks; tick++) {
			state.simulator.simulateBetween(
				state.user,
				state.metadata,
				configurations[tick & (configurations.length - 1)]
			);
			checksum += state.metadata.verifiedLastPositionX();
			checksum += state.metadata.verifiedLastPositionY();
			checksum += state.metadata.verifiedLastPositionZ();
			if ((tick + 1) % sampleInterval == 0) {
				peakUsedHeap = Math.max(peakUsedHeap, usedHeap());
				samples++;
			}
		}
		peakUsedHeap = Math.max(peakUsedHeap, usedHeap());
		return new MemoryRunResult(checksum, peakUsedHeap, samples);
	}

	private static long usedHeap() {
		Runtime runtime = Runtime.getRuntime();
		return runtime.totalMemory() - runtime.freeMemory();
	}

	private static void forceGc() {
		for (int attempt = 0; attempt < 3; attempt++) {
			System.gc();
			try {
				Thread.sleep(25L);
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private static String formatMillis(long nanos) {
		return format(nanos / 1_000_000.0);
	}

	private static String formatBytes(long bytes) {
		return format(bytes / 1024.0 / 1024.0) + " MiB";
	}

	private static String formatSignedBytes(long bytes) {
		String sign = bytes >= 0 ? "+" : "-";
		return sign + formatBytes(Math.abs(bytes));
	}

	private static String format(double value) {
		return String.format(Locale.ROOT, "%.3f", value);
	}

	private record MemoryRunResult(double checksum, long peakUsedHeap, int samples) {
	}

	private record BenchmarkState(
		User user, MovementMetadata metadata, Simulator simulator,
		MovementConfiguration[] configurations
	) {
		private static BenchmarkState create() {
			User user = createUser();
			MovementMetadata metadata = user.meta().movement();
			metadata.sneaking = true;
			Simulator simulator = Simulators.PLAYER;
			return new BenchmarkState(
				user,
				metadata,
				simulator,
				new MovementConfiguration[]{
					MovementConfiguration.blank().pressingW(),
					MovementConfiguration.blank().pressingA(),
					MovementConfiguration.blank().pressingS(),
					MovementConfiguration.blank().pressingD()
				}
			);
		}

		private static User createUser() {
			Collider collider = Colliders.anyCollider();
			FluidFlow waterflow = Fluids.anyWaterflow();
			SimpleCollider simpleCollider = Colliders.anySimpleCollider();
			World world = FakeWorldFactory.createWorld(
				(methodName, _) -> switch (methodName) {
					case "isChunkLoaded", "isChunkInUse" -> true;
					case "isThundering", "hasStorm" -> false;
					default -> null;
				}
			);
			Location location = new Location(world, 0, 50, 0);
			Player player = FakePlayerFactory.createPlayer(
				(methodName, _) -> switch (methodName) {
					case "getWorld" -> world;
					case "getLocation" -> location;
					case "getUniqueId" -> UUID.fromString("00000000-0000-0000-0000-000000000010");
					default -> null;
				}
			);
			MockFullBlockStaticPlane plane = MockFullBlockStaticPlane.createWithHorizontalPlaneAt(0);
			User user = UserFactory.createTestUserFor(player, (usr, key) -> switch (key) {
				case "collider" -> collider;
				case "waterflow" -> waterflow;
				case "simplifiedCollider" -> simpleCollider;
				case "blockCache" -> plane;
				case "protocolVersion" -> ProtocolMetadata.VER_1_21_2;
				default -> null;
			});
			UserRepository.manuallyRegisterUser(player, user);
			return user;
		}
	}

	private record BenchmarkOptions(int ticks, int warmupTicks, int sampleInterval) {
		private static BenchmarkOptions from(String[] args) {
			int ticks = args.length >= 1 ? parsePositiveInt(args[0], "ticks") : DEFAULT_TICKS;
			int warmupTicks = args.length >= 2
				? parsePositiveInt(args[1], "warmup ticks")
				: DEFAULT_WARMUP_TICKS;
			int sampleInterval = args.length >= 3
				? parsePositiveInt(args[2], "sample interval")
				: DEFAULT_SAMPLE_INTERVAL;
			return new BenchmarkOptions(ticks, warmupTicks, sampleInterval);
		}

		private static int parsePositiveInt(String text, String name) {
			try {
				int value = Integer.parseInt(text);
				if (value <= 0) {
					throw new IllegalArgumentException(name + " must be greater than zero");
				}
				return value;
			} catch (NumberFormatException exception) {
				throw new IllegalArgumentException(name + " must be an integer: " + text, exception);
			}
		}
	}
}
