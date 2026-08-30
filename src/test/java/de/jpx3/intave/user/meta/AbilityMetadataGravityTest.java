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

package de.jpx3.intave.user.meta;

import de.jpx3.intave.adapter.MinecraftVersion;
import de.jpx3.intave.adapter.MinecraftVersions;
import de.jpx3.intave.block.cache.MockFullBlockStaticPlane;
import de.jpx3.intave.player.attribute.Attribute;
import de.jpx3.intave.player.attribute.AttributeModifier;
import de.jpx3.intave.test.FakePlayerFactory;
import de.jpx3.intave.test.FakeWorldFactory;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserFactory;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static de.jpx3.intave.player.attribute.AttributeModifier.Operation.ADD_NUMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

final class AbilityMetadataGravityTest {
  @BeforeEach
  void setUp() {
    MinecraftVersion.setCurrent(MinecraftVersions.VER1_21_4);
  }

  @AfterEach
  void tearDown() {
    MinecraftVersion.setCurrent(MinecraftVersions.VER1_21_4);
  }

  @Test
  void doesNotRegisterGravityBefore1205() {
    MinecraftVersion.setCurrent(new MinecraftVersion("1.20.4"));

    AbilityMetadata abilities = new AbilityMetadata(FakePlayerFactory.createPlayer((ignored, arguments) -> null));

    assertNull(abilities.findAttribute("generic.gravity"));
  }

  @Test
  void registersGravityUnderIts1205Key() {
    MinecraftVersion.setCurrent(MinecraftVersions.VER1_20_5);

    AbilityMetadata abilities = new AbilityMetadata(FakePlayerFactory.createPlayer((ignored, arguments) -> null));
    Attribute gravity = abilities.findAttribute("generic.gravity");

    assertNotNull(gravity);
    assertEquals("generic.gravity", gravity.attributeKey());
    assertEquals(0.08D, abilities.attributeValue("generic.gravity"));
  }

  @Test
  void remapsGravityToIts1212Key() {
    MinecraftVersion.setCurrent(MinecraftVersions.VER1_21_2);

    AbilityMetadata abilities = new AbilityMetadata(FakePlayerFactory.createPlayer((ignored, arguments) -> null));
    Attribute gravity = abilities.findAttribute("generic.gravity");

    assertNotNull(gravity);
    assertEquals("gravity", gravity.attributeKey());
    assertSame(gravity, abilities.findAttribute("gravity"));
  }

  @Test
  void computesTrackedGravityBaseAndModifiers() {
    AbilityMetadata abilities = new AbilityMetadata(FakePlayerFactory.createPlayer((ignored, arguments) -> null));
    Attribute gravity = abilities.findAttribute("gravity");
    AttributeModifier modifier = AttributeModifier.newBuilder(
        UUID.fromString("8d87bbaa-35e1-4f37-99ed-396c91d8ed4b")
      )
      .withName("test-gravity")
      .withOperation(ADD_NUMBER)
      .withAmount(-0.02D)
      .build();

    abilities.modifyBaseValue("gravity", 0.06D);
    abilities.modifiersOf(gravity).add(modifier);

    assertEquals(0.04D, abilities.attributeValue("gravity"), 1.0E-12D);
  }

  @Test
  void exposesGravityWithTheClientAttributeBounds() {
    AbilityMetadata abilities = new AbilityMetadata(FakePlayerFactory.createPlayer((ignored, arguments) -> null));

    abilities.modifyBaseValue("gravity", -2.0D);
    assertEquals(-1.0D, abilities.gravity());

    abilities.modifyBaseValue("gravity", 2.0D);
    assertEquals(1.0D, abilities.gravity());

    abilities.modifyBaseValue("gravity", Double.NaN);
    assertEquals(-1.0D, abilities.gravity());
  }

  @Test
  void modernClientMovementUsesTrackedGravity() {
    User user = user(ProtocolMetadata.VER_1_21_4);
    user.meta().abilities().modifyBaseValue("gravity", 0.04D);

    user.meta().movement().updateMovement(0.0D, 1.0D, 0.0D, 0.0F, 0.0F, true, false);

    assertEquals(0.04D, user.meta().movement().gravity(), 1.0E-12D);
  }

  @Test
  void legacyClientMovementKeepsFixedGravity() {
    User user = user(ProtocolMetadata.VER_1_20_3);
    user.meta().abilities().modifyBaseValue("gravity", 0.04D);

    user.meta().movement().updateMovement(0.0D, 1.0D, 0.0D, 0.0F, 0.0F, true, false);

    assertEquals(0.08D, user.meta().movement().gravity(), 1.0E-12D);
  }

  @Test
  void slowFallingCapsPositiveGravityWithoutRaisingNegativeGravity() {
    assertEquals(0.01D, MovementMetadata.resolveGravity(0.04D, true, true));
    assertEquals(-0.2D, MovementMetadata.resolveGravity(-0.2D, true, true));
    assertEquals(0.04D, MovementMetadata.resolveGravity(0.04D, false, true));
  }

  private static User user(int protocolVersion) {
    World world = FakeWorldFactory.createWorld((methodName, arguments) -> switch (methodName) {
      case "isChunkLoaded", "isChunkInUse" -> true;
      case "isThundering", "hasStorm" -> false;
      default -> null;
    });
    Location location = new Location(world, 0.0D, 0.0D, 0.0D);
    Player player = FakePlayerFactory.createPlayer((methodName, arguments) -> switch (methodName) {
      case "getLocation" -> location;
      case "getWorld" -> world;
      default -> null;
    });
    MockFullBlockStaticPlane blocks = new MockFullBlockStaticPlane();
    return UserFactory.createTestUserFor(player, (ignored, key) -> switch (key) {
      case "blockCache" -> blocks;
      case "protocolVersion" -> protocolVersion;
      default -> null;
    });
  }
}
