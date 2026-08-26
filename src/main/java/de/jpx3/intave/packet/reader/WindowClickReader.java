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

package de.jpx3.intave.packet.reader;

import ac.intave.samples.event.InventoryActionEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BukkitConverters;
import de.jpx3.intave.adapter.MinecraftVersions;
import de.jpx3.intave.klass.Lookup;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserRepository;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WindowClickReader extends AbstractPacketReader {
  private static Class<?> nativeInventoryClickTypeClass;

  public InventoryClickType clickType() {
    if (MinecraftVersions.VER1_9_0.atOrAbove()) {
      return packet().getEnumModifier(
        InventoryClickType.class, nativeInventoryClickTypeClass()
      ).read(0);
    } else {
      Integer manualSlot = packet().getIntegers().readSafely(3);
      return InventoryClickType.values()[manualSlot];
    }
  }

  public int containerId() {
    return packet().getIntegers().readSafely(0);
  }

  public String clickedItemTypeIfPossible(Player player) {
    if (containerId() == 0 && slot() >= 0) {
      User user = UserRepository.userOf(player);
      List<String> items = user.meta().inventory().items();
      int slot = slot();
      return items == null || slot >= items.size() ? null : items.get(slot);
    } else {
      return null;
    }
  }

  public int slot() {
    int slotId = MinecraftVersions.VER1_17_1.atOrAbove() ? 2 : 1;
    Integer integer = packet().getIntegers().readSafely(slotId);
    if (integer == null) {
      return packet().getShorts().readSafely(0);
    }
    return integer;
  }

  public int button() {
    int buttonId = MinecraftVersions.VER1_17_1.atOrAbove() ? 3 : 2;
    Integer integer = packet().getIntegers().readSafely(buttonId);
    if (integer == null) {
      return packet().getBytes().readSafely(0);
    }
    return integer;
  }

  private static Class<?> nativeInventoryClickTypeClass() {
    if (nativeInventoryClickTypeClass == null) {
      nativeInventoryClickTypeClass = Lookup.serverClass("InventoryClickType");
    }
    return nativeInventoryClickTypeClass;
  }

  public InventoryActionEvent.Action action() {
    return actionOf(clickType(), slot(), button());
  }

  static InventoryActionEvent.Action actionOf(
    InventoryClickType clickType, int slot, int button
  ) {
    switch (clickType) {
      case PICKUP:
        if (slot == -999) {
          return button == 0
            ? InventoryActionEvent.Action.DROP_CURSOR_STACK
            : button == 1
              ? InventoryActionEvent.Action.DROP_CURSOR_ONE
              : InventoryActionEvent.Action.UNKNOWN;
        }
        return button == 0
          ? InventoryActionEvent.Action.CLICK_PRIMARY
          : button == 1
            ? InventoryActionEvent.Action.CLICK_SECONDARY
            : InventoryActionEvent.Action.UNKNOWN;
      case QUICK_MOVE:
        return InventoryActionEvent.Action.QUICK_MOVE;
      case SWAP:
        return button >= 0 && button <= 8
          ? InventoryActionEvent.Action.SWAP_HOTBAR
          : button == 40
            ? InventoryActionEvent.Action.SWAP_OFFHAND
            : InventoryActionEvent.Action.UNKNOWN;
      case CLONE:
        return InventoryActionEvent.Action.CLONE;
      case THROW:
        return button == 0
          ? InventoryActionEvent.Action.DROP_SLOT_ONE
          : button == 1
            ? InventoryActionEvent.Action.DROP_SLOT_STACK
            : InventoryActionEvent.Action.UNKNOWN;
      case QUICK_CRAFT:
        switch (button & 3) {
          case 0:
            return InventoryActionEvent.Action.DRAG_START;
          case 1:
            return InventoryActionEvent.Action.DRAG_ADD_SLOT;
          case 2:
            return InventoryActionEvent.Action.DRAG_END;
          default:
            return InventoryActionEvent.Action.UNKNOWN;
        }
      case PICKUP_ALL:
        return InventoryActionEvent.Action.COLLECT_TO_CURSOR;
      default:
        return InventoryActionEvent.Action.UNKNOWN;
    }
  }

  public int actionNumber() {
    StructureModifier<Integer> integers = packet().getIntegers();
    if (integers.size() == 4) {
      return integers.readSafely(3);
    } else {
      return packet().getShorts().readSafely(0);
    }
  }

  /** Legacy action number or modern container state ID. */
  public Integer revision() {
    if (MinecraftVersions.VER1_17_0.atOrAbove()) {
      return packet().getIntegers().readSafely(1);
    }
    return actionNumber();
  }

  public ItemStack itemStack() {
    return packet().getItemModifier().readSafely(0);
  }

  public boolean carriedItemKnown() {
    return MinecraftVersions.VER1_17_0.atOrAbove();
  }

  /** The client's predicted cursor stack on modern container-click packets. */
  public ItemStack carriedItem() {
    return carriedItemKnown() ? packet().getItemModifier().readSafely(0) : null;
  }

  /** The client's predicted changed slots on modern container-click packets. */
  public Map<Integer, ItemStack> predictedSlots() {
    if (!MinecraftVersions.VER1_17_0.atOrAbove()) {
      return Collections.emptyMap();
    }
    Object rawMap = null;
    for (Object value : packet().getModifier().getValues()) {
      if (value instanceof Map) {
        rawMap = value;
        break;
      }
    }
    if (!(rawMap instanceof Map)) {
      return Collections.emptyMap();
    }
    Map<Integer, ItemStack> converted = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : ((Map<?, ?>) rawMap).entrySet()) {
      if (!(entry.getKey() instanceof Number)) {
        continue;
      }
      Object value = entry.getValue();
      ItemStack item = value instanceof ItemStack
        ? (ItemStack) value
        : BukkitConverters.getItemStackConverter().getSpecific(value);
      converted.put(((Number) entry.getKey()).intValue(), item);
    }
    return converted;
  }

  public boolean isDrop() {
    if (MinecraftVersions.VER1_9_0.atOrAbove()) {
      return clickType() == InventoryClickType.THROW && slot() != -999;
    } else {
      return packet().getIntegers().read(3) == 4 && slot() != -999;
    }
  }

  public boolean missingItemStack() {
    switch (clickType()) {
      case QUICK_MOVE:
      case SWAP:
//      case PICKUP_ALL:
        return true;
      default:
        return false;
    }
  }

  public enum InventoryClickType {
    PICKUP,
    QUICK_MOVE,
    SWAP,
    CLONE,
    THROW,
    QUICK_CRAFT,
    PICKUP_ALL
  }
}
