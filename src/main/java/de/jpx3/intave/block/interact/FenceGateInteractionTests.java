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

package de.jpx3.intave.block.interact;

import de.jpx3.intave.adapter.MinecraftVersions;
import de.jpx3.intave.block.cache.BlockCache;
import de.jpx3.intave.block.cache.BlockCaches;
import de.jpx3.intave.block.type.MaterialSearch;
import de.jpx3.intave.block.variant.BlockVariantRegister;
import de.jpx3.intave.test.After;
import de.jpx3.intave.test.Before;
import de.jpx3.intave.test.BlockStorage;
import de.jpx3.intave.test.FakePlayerFactory;
import de.jpx3.intave.test.IntegrationTests;
import de.jpx3.intave.test.MockEmptyInventory;
import de.jpx3.intave.test.Severity;
import de.jpx3.intave.test.Test;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserFactory;
import de.jpx3.intave.user.UserRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class FenceGateInteractionTests extends IntegrationTests {
  private static final boolean LEGACY_VARIANTS = !MinecraftVersions.VER1_13_0.atOrAbove();

  private World world;
  private Block block;
  private BlockStorage priorMaterial;
  private Player player;
  private User user;
  private BlockCache blockCache;

  public FenceGateInteractionTests() {
    super("FGI");
  }

  @Before
  public void setup() {
    world = Bukkit.getWorlds().get(0);
    block = world.getBlockAt(0, 1, 0);
    priorMaterial = BlockStorage.store(block);
    player = FakePlayerFactory.createPlayer(
      (methodName, args) -> {
        switch (methodName) {
          case "getWorld":
            return world;
          case "getInventory":
            return new MockEmptyInventory();
          case "getLocation":
            return new Location(world, 0.5D, 1.0D, 0.5D);
          case "getUniqueId":
            return UUID.randomUUID();
          case "getActivePotionEffects":
            return Collections.emptyList();
        }
        return null;
      }
    );
    blockCache = BlockCaches.cacheForPlayer(player);
    user = UserFactory.createTestUserFor(player, (usr, key) -> {
      if ("blockCache".equals(key)) {
        return blockCache;
      }
      if ("protocolVersion".equals(key)) {
        return 0;
      }
      return null;
    });
    UserRepository.manuallyRegisterUser(player, user);
  }

  @Test(
    testCode = "A",
    severity = Severity.ERROR
  )
  public void fenceGateVariantsToggleOpenProperty() {
    int testedVariants = 0;
    for (Material fenceGate : fenceGateMaterials()) {
      if (!isUsableFenceGate(fenceGate)) {
        continue;
      }
      for (Integer variantId : BlockVariantRegister.variantIdsOf(fenceGate)) {
        Map<String, Comparable<?>> properties = BlockVariantRegister.propertiesOf(fenceGate, variantId);
        Comparable<?> openProperty = properties.get("open");
        if (!(openProperty instanceof Boolean)) {
          continue;
        }

        boolean open = (Boolean) openProperty;
        int interactedVariantId = BlockInteractionPatches.interact(user, fenceGate, variantId);
        assertTrue(interactedVariantId >= 0);

        Map<String, Comparable<?>> expectedProperties = new HashMap<>(properties);
        expectedProperties.put("open", !open);
        Map<String, Comparable<?>> actualProperties =
          BlockVariantRegister.propertiesOf(fenceGate, interactedVariantId);
        if (LEGACY_VARIANTS) {
          expectedProperties.remove("in_wall");
          actualProperties.remove("in_wall");
        }
        assertEquals(expectedProperties, actualProperties);
        testedVariants++;
      }
    }
    assertTrue(testedVariants > 0);
  }

  @Test(
    testCode = "B",
    severity = Severity.ERROR
  )
  public void fenceGateInteractionOverridesBlockCache() {
    FenceGateVariant variant = closedFenceGateVariant();
    block.setType(variant.type, false);

    blockCache.override(
      world,
      block.getX(), block.getY(), block.getZ(),
      variant.type, variant.closedVariantId,
      "FENCE_GATE_TEST"
    );
    assertEquals(
      variant.closedVariantId,
      blockCache.variantIndexAt(block.getX(), block.getY(), block.getZ())
    );

    assertTrue(BlockInteractionPatches.interact(
      user, world, blockCache,
      block.getX(), block.getY(), block.getZ(),
      "FENCE_GATE_TEST"
    ));
    assertEquals(
      variant.openVariantId,
      blockCache.variantIndexAt(block.getX(), block.getY(), block.getZ())
    );

    assertTrue(BlockInteractionPatches.interact(
      user, world, blockCache,
      block.getX(), block.getY(), block.getZ(),
      "FENCE_GATE_TEST"
    ));
    assertEquals(
      variant.reclosedVariantId,
      blockCache.variantIndexAt(block.getX(), block.getY(), block.getZ())
    );
  }

  @After
  public void teardown() {
    if (priorMaterial != null) {
      priorMaterial.restore();
    }
    if (blockCache != null && block != null) {
      blockCache.invalidateAll();
    }
    if (player != null) {
      UserRepository.unregisterUser(player);
    }
  }

  private FenceGateVariant closedFenceGateVariant() {
    for (Material fenceGate : fenceGateMaterials()) {
      if (!isUsableFenceGate(fenceGate)) {
        continue;
      }
      for (Integer variantId : BlockVariantRegister.variantIdsOf(fenceGate)) {
        Map<String, Comparable<?>> properties = BlockVariantRegister.propertiesOf(fenceGate, variantId);
        if (!Boolean.FALSE.equals(properties.get("open"))) {
          continue;
        }

        int openVariantId = BlockInteractionPatches.interact(user, fenceGate, variantId);
        if (openVariantId < 0) {
          continue;
        }
        int reclosedVariantId = BlockInteractionPatches.interact(user, fenceGate, openVariantId);
        if (reclosedVariantId >= 0) {
          return new FenceGateVariant(fenceGate, variantId, openVariantId, reclosedVariantId);
        }
      }
    }
    throw new AssertionError("No closed fence-gate variant found");
  }

  private Set<Material> fenceGateMaterials() {
    return MaterialSearch.materialsThatContain("FENCE_GATE");
  }

  private boolean isUsableFenceGate(Material material) {
    return material.isBlock()
      && FenceGateInteractionInteractionPatch.isFenceGate(material)
      && !BlockVariantRegister.variantIdsOf(material).isEmpty();
  }

  private static final class FenceGateVariant {
    private final Material type;
    private final int closedVariantId;
    private final int openVariantId;
    private final int reclosedVariantId;

    private FenceGateVariant(
      Material type,
      int closedVariantId,
      int openVariantId,
      int reclosedVariantId
    ) {
      this.type = type;
      this.closedVariantId = closedVariantId;
      this.openVariantId = openVariantId;
      this.reclosedVariantId = reclosedVariantId;
    }
  }
}
