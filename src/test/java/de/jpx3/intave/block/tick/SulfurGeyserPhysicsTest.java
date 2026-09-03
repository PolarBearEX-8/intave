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

package de.jpx3.intave.block.tick;

import de.jpx3.intave.adapter.MinecraftVersion;
import de.jpx3.intave.adapter.MinecraftVersions;
import de.jpx3.intave.check.movement.physics.environment.MockSimulationEnvironment;
import de.jpx3.intave.check.movement.physics.environment.SimulationEnvironment;
import de.jpx3.intave.share.BoundingBox;
import de.jpx3.intave.share.Motion;
import de.jpx3.intave.share.Position;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static de.jpx3.intave.user.meta.ProtocolMetadata.VER_26_1_1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SulfurGeyserPhysicsTest {
	private static final BoundingBox INSIDE_FIRST_BLOCK = BoundingBox.fromBounds(
		0.2D, 1.0D, 0.2D,
		0.8D, 2.8D, 0.8D
	);
	private static final BoundingBox ONE_WATER_BLOCK_LAUNCH_AREA =
		SulfurGeyserPhysics.launchArea(0, 0, 0, 6);

	@BeforeAll
	static void setUpVersion() {
		MinecraftVersion.setCurrent(MinecraftVersions.VER26_2);
	}

	@Test
	void clientsBefore262DoNotSimulateGeysers() {
		MockSimulationEnvironment environment = new MockSimulationEnvironment();
		environment.addFallDistance(4.0D);
		Motion input = new Motion(0.1D, 0.0D, -0.2D);
		User user = UserFactory.createFallback();
		user.meta().protocol().setProtocolVersion(VER_26_1_1);

		Motion result = SulfurGeyserPhysics.applyAfterPlayerTick(
			user, environment, Position.immutableEmpty(), input
		);

		assertSame(input, result);
		assertEquals(4.0D, environment.fallDistance(), 0.0D);
	}

	@Test
	void onlyEruptingAndContinuousStatesAreActiveGeysers() {
		assertTrue(SulfurGeyserPhysics.PotentSulfurState.ERUPTING.activeGeyser());
		assertTrue(SulfurGeyserPhysics.PotentSulfurState.CONTINUOUS.activeGeyser());
		assertFalse(SulfurGeyserPhysics.PotentSulfurState.DORMANT.activeGeyser());
		assertFalse(SulfurGeyserPhysics.PotentSulfurState.WET.activeGeyser());
		assertFalse(SulfurGeyserPhysics.PotentSulfurState.DRY.activeGeyser());
	}

	@Test
	void activeGeyserAddsTheClientsFloatLaunchForceAfterPlayerTick() {
		MockSimulationEnvironment environment = new MockSimulationEnvironment();
		environment.addFallDistance(4.0D);
		Motion input = new Motion(0.1D, 0.1D, -0.2D);

		Motion result = apply(
			environment, INSIDE_FIRST_BLOCK, input,
			ONE_WATER_BLOCK_LAUNCH_AREA, 1, false, false
		);

		assertEquals(input.motionX(), result.motionX(), 0.0D);
		assertEquals(0.1D + (double) 0.2F, result.motionY(), 0.0D);
		assertEquals(input.motionZ(), result.motionZ(), 0.0D);
		assertEquals(1.0D, environment.fallDistance(), 0.0D);
	}

	@Test
	void launchSpeedComparisonUsesTheStrictWaterDepthCap() {
		double limit = 0.3F + 0.1D;
		Motion atLimit = new Motion(0.0D, limit, 0.0D);

		Motion unchanged = apply(
			new MockSimulationEnvironment(), INSIDE_FIRST_BLOCK, atLimit,
			ONE_WATER_BLOCK_LAUNCH_AREA, 1, false, false
		);
		Motion launched = apply(
			new MockSimulationEnvironment(), INSIDE_FIRST_BLOCK,
			new Motion(0.0D, Math.nextDown(limit), 0.0D),
			ONE_WATER_BLOCK_LAUNCH_AREA, 1, false, false
		);

		assertSame(atLimit, unchanged);
		assertEquals(Math.nextDown(limit) + (double) 0.2F, launched.motionY(), 0.0D);
	}

	@Test
	void fourWaterBlocksProduceA24BlockLaunchColumn() {
		BoundingBox launchArea = SulfurGeyserPhysics.launchArea(0, 0, 0, 24);
		BoundingBox insideTop = BoundingBox.fromBounds(
			0.2D, 24.99D, 0.2D,
			0.8D, 26.79D, 0.8D
		);
		BoundingBox touchingTop = BoundingBox.fromBounds(
			0.2D, 25.0D, 0.2D,
			0.8D, 26.8D, 0.8D
		);

		Motion launched = apply(
			new MockSimulationEnvironment(), insideTop, Motion.newEmpty(),
			launchArea, 4, false, false
		);
		Motion touchingInput = Motion.newEmpty();
		Motion touching = apply(
			new MockSimulationEnvironment(), touchingTop, touchingInput,
			launchArea, 4, false, false
		);

		assertEquals((double) 0.2F, launched.motionY(), 0.0D);
		assertSame(touchingInput, touching);
	}

	@Test
	void launchColumnEndsAtTheObstructionCutoff() {
		BoundingBox launchArea = SulfurGeyserPhysics.launchArea(0, 0, 0, 3);
		BoundingBox inside = BoundingBox.fromBounds(
			0.2D, 3.99D, 0.2D,
			0.8D, 5.79D, 0.8D
		);
		BoundingBox touching = BoundingBox.fromBounds(
			0.2D, 4.0D, 0.2D,
			0.8D, 5.8D, 0.8D
		);

		Motion launched = apply(
			new MockSimulationEnvironment(), inside, Motion.newEmpty(),
			launchArea, 1, false, false
		);
		Motion touchingInput = Motion.newEmpty();
		Motion unchanged = apply(
			new MockSimulationEnvironment(), touching, touchingInput,
			launchArea, 1, false, false
		);

		assertEquals((double) 0.2F, launched.motionY(), 0.0D);
		assertSame(touchingInput, unchanged);
	}

	@Test
	void flyingAndPassengerPlayersAreNotLaunched() {
		Motion flyingInput = Motion.newEmpty();
		Motion passengerInput = Motion.newEmpty();

		Motion flying = apply(
			new MockSimulationEnvironment(), INSIDE_FIRST_BLOCK, flyingInput,
			ONE_WATER_BLOCK_LAUNCH_AREA, 1, true, false
		);
		Motion passenger = apply(
			new MockSimulationEnvironment(), INSIDE_FIRST_BLOCK, passengerInput,
			ONE_WATER_BLOCK_LAUNCH_AREA, 1, false, true
		);

		assertSame(flyingInput, flying);
		assertSame(passengerInput, passenger);
	}

	@Test
	void fallDistanceCapRunsBeforeTheLaunchExclusions() {
		MockSimulationEnvironment flyingEnvironment = new MockSimulationEnvironment();
		flyingEnvironment.addFallDistance(4.0D);
		Motion input = new Motion(0.0D, -0.4D, 0.0D);

		Motion result = apply(
			flyingEnvironment, INSIDE_FIRST_BLOCK, input,
			ONE_WATER_BLOCK_LAUNCH_AREA, 1, true, false
		);

		assertSame(input, result);
		assertEquals(1.0D, flyingEnvironment.fallDistance(), 0.0D);
	}

	@Test
	void fallDistanceComparisonIsStrictAtNegativeHalfMotion() {
		MockSimulationEnvironment environment = new MockSimulationEnvironment();
		environment.addFallDistance(4.0D);

		Motion result = apply(
			environment, INSIDE_FIRST_BLOCK,
			new Motion(0.0D, -0.5D, 0.0D),
			ONE_WATER_BLOCK_LAUNCH_AREA, 1, false, false
		);

		assertEquals(-0.5D + (double) 0.2F, result.motionY(), 0.0D);
		assertEquals(4.0D, environment.fallDistance(), 0.0D);
	}

	@Test
	void fallDistanceMutationRemainsBranchLocalUntilCommit() {
		MockSimulationEnvironment root = new MockSimulationEnvironment();
		root.addFallDistance(4.0D);
		SimulationEnvironment candidate = root.mutableView();

		SulfurGeyserPhysics.applyGeyserLaunch(
			candidate, INSIDE_FIRST_BLOCK, new Motion(0.0D, -0.4D, 0.0D),
			ONE_WATER_BLOCK_LAUNCH_AREA, 1, false, false
		);

		assertEquals(4.0D, root.fallDistance(), 0.0D);
		assertEquals(1.0D, candidate.fallDistance(), 0.0D);
		candidate.commitTo(root);
		assertEquals(1.0D, root.fallDistance(), 0.0D);
	}

	private static Motion apply(
		SimulationEnvironment environment,
		BoundingBox entityBox,
		Motion motion,
		BoundingBox launchArea,
		int waterBlocks,
		boolean flying,
		boolean passenger
	) {
		return SulfurGeyserPhysics.applyGeyserLaunch(
			environment, entityBox, motion, launchArea,
			waterBlocks, flying, passenger
		);
	}

}
