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

package de.jpx3.intave.cloud.protocol;

import com.google.common.collect.Maps;
import de.jpx3.intave.cloud.protocol.listener.Clientbound;
import de.jpx3.intave.cloud.protocol.listener.Serverbound;
import de.jpx3.intave.cloud.protocol.packets.ClientboundCombatModifier;
import de.jpx3.intave.cloud.protocol.packets.ClientboundSetTrustfactor;
import de.jpx3.intave.cloud.protocol.packets.ClientboundViolation;
import de.jpx3.intave.cloud.protocol.packets.ServerboundReport;
import de.jpx3.intave.cloud.protocol.packets.base.*;
import de.jpx3.intave.cloud.protocol.packets.player.*;
import de.jpx3.intave.cloud.protocol.packets.sampling.ClientboundSetSamplingState;
import de.jpx3.intave.cloud.protocol.packets.sampling.ServerboundPassSample;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PacketRegistry {
  private static final Map<Direction, Map<Class<? extends Packet<?>>, String>> nameByPacket = Maps.newHashMap();
  private static final Map<Direction, Map<String, Class<? extends Packet<?>>>> packetByName = Maps.newEnumMap(Direction.class);
  private static final Map<Direction, Map<String, PacketSpecification>> specifications = Maps.newEnumMap(Direction.class);

  static {
    registerClientbound(ClientboundConfirmAttestations.class);
    registerClientbound(ClientboundDisconnect.class);
    registerClientbound(ClientboundCombatModifier.class);
    registerClientbound(ClientboundHello.class);
    registerClientbound(ClientboundSendMessage.class);
    registerClientbound(ClientboundSetTrustfactor.class);
    registerClientbound(ClientboundViolation.class);
    registerClientbound(ClientboundKeepAlive.class);
    registerClientbound(ClientboundSetSamplingState.class);
    registerClientbound(ClientboundSetPlayerId.class);
    registerClientbound(ClientboundClarifyUnknownPlayerId.class);

    registerServerbound(ServerboundConfirmEncryption.class);
    registerServerbound(ServerboundReport.class);
    registerServerbound(ServerboundHello.class);
    registerServerbound(ServerboundPassSample.class);
    registerServerbound(ServerboundPlayerLogin.class);
    registerServerbound(ServerboundPlayerLogout.class);
    registerServerbound(ServerboundKeepAlive.class);
  }
  
  private static void registerClientbound(Class<? extends Packet<?>> packetClass) {
    register(Direction.CLIENTBOUND, packetClass);
  }

  private static void registerServerbound(Class<? extends Packet<?>> packetClass) {
    register(Direction.SERVERBOUND, packetClass);
  }

  private static void register(Direction direction, Class<? extends Packet<?>> packetClass) {
    try {
      Packet<?> packet = packetClass.newInstance();
      String packetName = packet.name();
      if (direction != packet.direction()) {
        throw new IllegalArgumentException("Packet " + packetName + " has wrong direction");
      }
      PacketSpecification packetSpecification = PacketSpecification.from(packet);
      nameByPacket.computeIfAbsent(direction, x -> new HashMap<>()).put(packetClass, packetName);
      packetByName.computeIfAbsent(direction, x -> new HashMap<>()).put(packetName, packetClass);
      specifications.computeIfAbsent(direction, x -> new HashMap<>()).put(packetName, packetSpecification);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Packet<?> fromName(Direction direction, String name) {
    Map<String, Class<? extends Packet<?>>> packets = packetByName.get(direction);
    Class<? extends Packet<?>> packetClass =
      packets == null ? null : packets.get(name);
    if (packetClass == null) {
      throw new IllegalArgumentException(
        "Unknown " + direction.name().toLowerCase() + " cloud packet name '"
          + name + "'; registered names are "
          + (packets == null ? "[]" : packets.keySet())
      );
    }
    try {
      return packetClass.newInstance();
    } catch (Exception e) {
      throw new IllegalStateException(
        "Unable to construct " + direction.name().toLowerCase()
          + " cloud packet '" + name + "' (" + packetClass.getName() + ")",
        e
      );
    }
  }

  public static String clientboundName(Class<? extends Packet<Clientbound>> packetClass) {
    return nameByPacket.get(Direction.CLIENTBOUND).get(packetClass);
  }

  public static String serverboundName(Class<? extends Packet<Serverbound>> packetClass) {
    return nameByPacket.get(Direction.SERVERBOUND).get(packetClass);
  }

  public static Set<String> packetNamesOf(Direction direction) {
    return new HashSet<>(packetByName.get(direction).keySet());
  }

  public static Packet<?> fromAssignedId(ProtocolSpecification protocol, Direction direction, int id) {
    Map<Integer, String> idToName = protocol.packetIdsOf(direction);
    String packetName = idToName == null ? null : idToName.get(id);
    if (packetName == null) {
      throw new IllegalArgumentException(
        "Unknown " + direction.name().toLowerCase() + " cloud packet id " + id
          + "; negotiated ids are " + (idToName == null ? "{}" : idToName)
      );
    }
    return fromName(direction, packetName);
  }

  public static Map<String, PacketSpecification> packetSpecsFor(Direction direction) {
    return specifications.computeIfAbsent(direction, x -> new HashMap<>());
  }
}
