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

import de.jpx3.intave.block.type.MaterialSearch;
import de.jpx3.intave.user.User;
import org.bukkit.Material;

import java.util.Set;

final class TrapdoorInteractionInteractionPatch implements BlockInteractionPatch {
  private static final Set<Material> TRAPDOOR_BLOCKS = MaterialSearch.materialsThatContain("TRAP_DOOR", "TRAPDOOR");

  @Override
  public boolean matches(Material material) {
    return isTrapdoor(material);
  }

  @Override
  public int interact(User user, Material material, int variant) {
    if (!canOpenByHand(material)) {
      return NO_CHANGE;
    }
    return BlockInteractionPatch.toggleBooleanProperty(material, variant, "open");
  }

  static boolean isTrapdoor(Material material) {
    return TRAPDOOR_BLOCKS.contains(material);
  }

  private static boolean canOpenByHand(Material material) {
    return !material.name().contains("IRON");
  }
}
