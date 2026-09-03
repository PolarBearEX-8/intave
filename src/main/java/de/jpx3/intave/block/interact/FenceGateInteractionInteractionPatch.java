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
import de.jpx3.intave.block.type.MaterialSearch;
import de.jpx3.intave.user.User;
import org.bukkit.Material;

import java.util.Set;

final class FenceGateInteractionInteractionPatch implements BlockInteractionPatch {
  private static final int LEGACY_OPEN_BIT = 4;
  private static final boolean LEGACY_VARIANTS = !MinecraftVersions.VER1_13_0.atOrAbove();
  private static final Set<Material> FENCE_GATE_BLOCKS = MaterialSearch.materialsThatContain("FENCE_GATE");

  @Override
  public boolean matches(Material material) {
    return isFenceGate(material);
  }

  static boolean isFenceGate(Material material) {
    return FENCE_GATE_BLOCKS.contains(material);
  }

  @Override
  public int interact(User user, Material material, int variant) {
    if (LEGACY_VARIANTS) {
      return variant ^ LEGACY_OPEN_BIT;
    }
    return BlockInteractionPatch.toggleBooleanProperty(material, variant, "open");
  }
}
