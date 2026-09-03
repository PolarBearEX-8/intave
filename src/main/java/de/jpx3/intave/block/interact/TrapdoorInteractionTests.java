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

import de.jpx3.intave.block.cache.BlockCache;
import de.jpx3.intave.block.cache.BlockCaches;
import de.jpx3.intave.block.type.MaterialSearch;
import de.jpx3.intave.block.variant.BlockVariantRegister;
import de.jpx3.intave.test.*;
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

public final class TrapdoorInteractionTests extends IntegrationTests {
  private World world;
  private Block block;
  private BlockStorage priorMaterial;
  private Player player;
  private User user;
  private BlockCache blockCache;

  public TrapdoorInteractionTests() {
    super("TDI");
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
  public void trapdoorVariantsToggleByProperties() {
    int testedVariants = 0;
    for (Material trapdoor : trapdoorMaterials()) {
      if (!isHandOpenableTrapdoor(trapdoor)) {
        continue;
      }
      for (Integer variantId : BlockVariantRegister.variantIdsOf(trapdoor)) {
        Map<String, Comparable<?>> properties = BlockVariantRegister.propertiesOf(trapdoor, variantId);
        Comparable<?> open = properties.get("open");
        if (!(open instanceof Boolean)) {
          continue;
        }
        int toggledVariantId = BlockInteractionPatches.interact(user, trapdoor, variantId);
        assertTrue(toggledVariantId >= 0);

        Map<String, Comparable<?>> expectedProperties = new HashMap<>(properties);
        expectedProperties.put("open", !((Boolean) open));
        assertEquals(expectedProperties, BlockVariantRegister.propertiesOf(trapdoor, toggledVariantId));
        assertEquals(variantId, BlockInteractionPatches.interact(user, trapdoor, toggledVariantId));
        testedVariants++;
      }
    }
    assertTrue(testedVariants > 0);
  }

  @Test(
    testCode = "B",
    severity = Severity.ERROR
  )
  public void trapdoorInteractionOverridesBlockCache() {
    TrapdoorVariant variant = closedHandOpenableTrapdoorVariant();
    block.setType(variant.type, false);

    blockCache.override(world, block.getX(), block.getY(), block.getZ(), variant.type, variant.variantId, "TRAPDOOR_TEST");
    assertEquals(variant.variantId, blockCache.variantIndexAt(block.getX(), block.getY(), block.getZ()));

    assertTrue(BlockInteractionPatches.interact(
      user, world, blockCache, block.getX(), block.getY(), block.getZ(), "TRAPDOOR_TEST"
    ));
    assertEquals(variant.toggledVariantId, blockCache.variantIndexAt(block.getX(), block.getY(), block.getZ()));
    assertEquals(Boolean.TRUE, BlockVariantRegister.propertiesOf(variant.type, blockCache.variantIndexAt(block.getX(), block.getY(), block.getZ())).get("open"));

    assertTrue(BlockInteractionPatches.interact(
      user, world, blockCache, block.getX(), block.getY(), block.getZ(), "TRAPDOOR_TEST"
    ));
    assertEquals(variant.variantId, blockCache.variantIndexAt(block.getX(), block.getY(), block.getZ()));
  }

  @Test(
    testCode = "C",
    severity = Severity.ERROR
  )
  public void ironTrapdoorsDoNotToggleByHand() {
    for (Material trapdoor : trapdoorMaterials()) {
      if (trapdoor.name().contains("IRON")) {
        for (Integer variantId : BlockVariantRegister.variantIdsOf(trapdoor)) {
          Map<String, Comparable<?>> properties = BlockVariantRegister.propertiesOf(trapdoor, variantId);
          if (properties.get("open") instanceof Boolean) {
            assertEquals(-1, BlockInteractionPatches.interact(user, trapdoor, variantId));
            return;
          }
        }
      }
    }
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

  private TrapdoorVariant closedHandOpenableTrapdoorVariant() {
    for (Material trapdoor : trapdoorMaterials()) {
      if (!isHandOpenableTrapdoor(trapdoor)) {
        continue;
      }
      for (Integer variantId : BlockVariantRegister.variantIdsOf(trapdoor)) {
        Map<String, Comparable<?>> properties = BlockVariantRegister.propertiesOf(trapdoor, variantId);
        if (!Boolean.FALSE.equals(properties.get("open"))) {
          continue;
        }
        int toggledVariantId = BlockInteractionPatches.interact(user, trapdoor, variantId);
        if (toggledVariantId >= 0) {
          return new TrapdoorVariant(trapdoor, variantId, toggledVariantId);
        }
      }
    }
    throw new AssertionError("No hand-openable trapdoor variant with open=false found");
  }

  private Set<Material> trapdoorMaterials() {
    return MaterialSearch.materialsThatContain("TRAP_DOOR", "TRAPDOOR");
  }

  private boolean isHandOpenableTrapdoor(Material material) {
    return material.isBlock()
      && TrapdoorInteractionInteractionPatch.isTrapdoor(material)
      && !material.name().contains("IRON")
      && !BlockVariantRegister.variantIdsOf(material).isEmpty();
  }

  private static final class TrapdoorVariant {
    private final Material type;
    private final int variantId;
    private final int toggledVariantId;

    private TrapdoorVariant(Material type, int variantId, int toggledVariantId) {
      this.type = type;
      this.variantId = variantId;
      this.toggledVariantId = toggledVariantId;
    }
  }
}
