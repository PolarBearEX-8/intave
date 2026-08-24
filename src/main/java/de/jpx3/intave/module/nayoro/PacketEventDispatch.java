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

package de.jpx3.intave.module.nayoro;

import ac.intave.samples.event.*;
import ac.intave.samples.share.Item;
import ac.intave.samples.share.Position;
import ac.intave.samples.share.Rotation;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.jpx3.intave.module.linker.packet.ListenerPriority;
import de.jpx3.intave.module.linker.packet.PacketEventSubscriber;
import de.jpx3.intave.module.linker.packet.PacketId;
import de.jpx3.intave.module.linker.packet.PacketSubscription;
import de.jpx3.intave.packet.reader.*;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserRepository;
import de.jpx3.intave.user.meta.MovementMetadata;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static ac.intave.samples.event.WindowActionEvent.Action.CLOSE;
import static ac.intave.samples.event.WindowActionEvent.Action.INFER_OPEN;
import static de.jpx3.intave.check.movement.physics.environment.MoveMetric.TELEPORT;
import static de.jpx3.intave.module.linker.packet.ListenerPriority.LOWEST;
import static de.jpx3.intave.module.linker.packet.PacketId.Client.*;
import static de.jpx3.intave.module.linker.packet.PacketId.Client.POSITION;
import static de.jpx3.intave.module.linker.packet.PacketId.Client.VEHICLE_MOVE;
import static de.jpx3.intave.module.linker.packet.PacketId.Server.*;

public final class PacketEventDispatch implements PacketEventSubscriber {
  private final BiConsumer<? super User, ? super Event> eventEmitter;

  public PacketEventDispatch(BiConsumer<? super User, ? super Event> eventEmitter) {
    this.eventEmitter = eventEmitter;
  }

  @PacketSubscription(
    packetsIn = {
      ARM_ANIMATION
    }
  )
  public void onClick(PacketEvent event) {
    Player player = event.getPlayer();
    User user = UserRepository.userOf(player);
    // The samples factory returns a shared singleton, but recording offsets are event-local.
    ClickEvent clickEvent = new ClickEvent();
    eventEmitter.accept(user, clickEvent);
  }

  @PacketSubscription(
    priority = LOWEST,
    packetsIn = {
      ATTACK_ENTITY, USE_ENTITY
    }
  )
  public void onUse(PacketEvent event) {
    Player player = event.getPlayer();
    User user = UserRepository.userOf(player);
    PacketContainer packet = event.getPacket();
    EntityUseReader reader = PacketReaders.readerOf(packet);
    EnumWrappers.EntityUseAction useAction = reader.useAction();
    if (useAction == EnumWrappers.EntityUseAction.ATTACK) {
      int attackerId = player.getEntityId();
      int targetId = reader.entityId();
      AttackEvent attackEvent = AttackEvent.create(attackerId, targetId);
      eventEmitter.accept(user, attackEvent);
    }
    reader.release();
  }

  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packetsIn = {
      FLYING, LOOK, POSITION, POSITION_LOOK, VEHICLE_MOVE
    }
  )
  public void receiveMovement(PacketEvent event) {
    Player player = event.getPlayer();
    User user = UserRepository.userOf(player);
    MovementMetadata movement = user.meta().movement();
    double x = movement.positionX;
    double y = movement.positionY;
    double z = movement.positionZ;
    float yaw = movement.rotationYaw;
    float pitch = movement.rotationPitch;
    int keyStrafe = movement.keyStrafe;
    int keyForward = movement.keyForward;

    boolean collidedHorizontally = movement.collidedHorizontally;
    boolean collidedVertically = movement.collidedVertically || movement.onGround();
    boolean inWater = movement.inWater();
    boolean inLava = movement.inLava();

    boolean inVehicle = movement.isInVehicle();
    boolean sneaking = movement.isSneaking();
    boolean recentlyTeleported = movement.ticksPast(TELEPORT) <= 3;
    boolean jumped = movement.physicsJumped;

    PlayerMoveEvent movementEvent = PlayerMoveEvent.create(
      keyStrafe, keyForward,
      new Position(x, y, z), new Rotation(yaw, pitch),
      collidedHorizontally, collidedVertically, inWater, inLava,
      inVehicle, sneaking, recentlyTeleported, jumped
    );
    eventEmitter.accept(user, movementEvent);
  }

  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packetsIn = {
      HELD_ITEM_SLOT_IN
    }
  )
  public void receiveHeldItemSlot(PacketEvent event) {
    Player player = event.getPlayer();
    User user = UserRepository.userOf(player);
    int slot = event.getPacket().getIntegers().read(0);
    ItemStack item = player.getInventory().getItem(slot);
    Material type;
    int amount;
    if (item != null) {
      type = item.getType();
      amount = item.getAmount();
    } else {
      type = Material.AIR;
      amount = 0;
    }
    SlotSwitchEvent slotSwitchEvent = SlotSwitchEvent.create(
      slot, type.name(), amount
    );
    eventEmitter.accept(user, slotSwitchEvent);
  }

  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packetsIn = {
      WINDOW_CLICK
    }
  )
  public void receiveWindowClick(
    User user, WindowClickReader reader
  ) {
    boolean assumeWindowOpen = user.meta().connection().assumeWindowOpen;
    if (!assumeWindowOpen) {
      user.meta().connection().assumeWindowOpen = true;
      WindowActionEvent openEvent = WindowActionEvent.create(
        INFER_OPEN, SampleTypes.items(user.player().getInventory().getArmorContents())
      );
      eventEmitter.accept(user, openEvent);
    }
    WindowClickEvent clickEvent = WindowClickEvent.create(
      reader.containerId(), reader.slot(), reader.clickType().ordinal(), reader.button(), reader.actionNumber()
    );
    eventEmitter.accept(user, clickEvent);
  }

  @PacketSubscription(
    priority = ListenerPriority.LOW,
    packetsIn = {
      PacketId.Client.CLOSE_WINDOW
    }
  )
  public void receiveWindowClose(PacketEvent event) {
    Player player = event.getPlayer();
    User user = UserRepository.userOf(player);
    WindowActionEvent closeEvent = WindowActionEvent.create(
      CLOSE, SampleTypes.items(user.player().getInventory().getArmorContents())
    );
    eventEmitter.accept(user, closeEvent);
    user.meta().connection().assumeWindowOpen = false;
  }

  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packetsOut = {
      OPEN_WINDOW
    }
  )
  public void sentWindowOpen(
    User user, WindowOpenReader reader
  ) {
    int slots = reader.slots();
    user.meta().connection().nextWindowOpenSlots = slots;
  }

  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packetsOut = {
      WINDOW_ITEMS, SET_SLOT
    }
  )
  public void sendWindowItems(
    User user, WindowItemReader reader
  ) {
    int container = reader.windowId();
    int slots = user.meta().connection().nextWindowOpenSlots;
    if (slots == 0) {
      slots = 9 * 3;
    }
    if (container != 0) {
      user.meta().connection().nextWindowOpenSlots = 0;
    }

    // inventory
    slots += 4 * 9;
    Map<Integer, ItemStack> items = reader.itemMap();
    WindowItemsEvent event = WindowItemsEvent.create(
      container, slots,
      items.entrySet().stream().map(integerItemStackEntry -> new Map.Entry<Integer, Item>() {
        @Override
        public Integer getKey() {
          return integerItemStackEntry.getKey();
        }

        @Override
        public Item getValue() {
          return SampleTypes.item(integerItemStackEntry.getValue());
        }

        @Override
        public Item setValue(Item value) {
          throw new UnsupportedOperationException("setValue is not supported");
        }
      }).collect(Collectors.toMap(
        Map.Entry::getKey, Map.Entry::getValue
      ))
    );
    eventEmitter.accept(user, event);
  }
}
