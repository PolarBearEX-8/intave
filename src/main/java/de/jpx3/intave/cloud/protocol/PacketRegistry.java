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
import de.jpx3.intave.cloud.protocol.packets.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PacketRegistry {
  private static final Map<Direction, Map<Class<? extends Packet<?>>, String>> nameByPacket = Maps.newHashMap();
  private static final Map<Direction, Map<String, Class<? extends Packet<?>>>> packetByName = Maps.newEnumMap(Direction.class);
  private static final Map<Direction, Map<String, PacketSpecification>> specifications = Maps.newEnumMap(Direction.class);

  static {
    registerClientbound(ClientboundDisconnect.class);
    registerClientbound(ClientboundCombatModifier.class);
    registerClientbound(ClientboundDownloadStorage.class);
    registerClientbound(ClientboundHello.class);
    registerClientbound(ClientboundSetTrustfactor.class);
    registerClientbound(ClientboundViolation.class);
    registerClientbound(ClientboundKeepAlive.class);
    registerClientbound(ClientboundLogReceive.class);
    registerClientbound(ClientboundSampleTransmissionAcknowledgement.class);
    registerClientbound(ClientboundCommand.class);
    registerClientbound(ClientboundInquiryResponse.class);

    registerServerbound(ServerboundConfirmEncryption.class);
    registerServerbound(ServerboundHello.class);
    registerServerbound(ServerboundPassNayoro.class);
    registerServerbound(ServerboundPlayerPlayStateChange.class);
    registerServerbound(ServerboundRequestStorage.class);
    registerServerbound(ServerboundRequestTrustfactor.class);
    registerServerbound(ServerboundUploadStorage.class);
    registerServerbound(ServerboundKeepAlive.class);
    registerServerbound(ServerboundUploadLogs.class);
    registerServerbound(ServerboundSampleTransmissionRequest.class);
    registerServerbound(ServerboundSampleCompleted.class);
    registerServerbound(ServerboundStatusInquiry.class);
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
    try {
      return packetByName.get(direction).get(name).newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
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
    try {
      String packetName = idToName.get(id);
      return fromName(direction, packetName);
    } catch (Exception exception) {
      System.out.println("Failed to find packet id " + id + " direction " + direction + " (avail: " + idToName+")");
      throw new RuntimeException(exception);
    }
  }

  public static Map<String, PacketSpecification> packetSpecsFor(Direction direction) {
    return specifications.computeIfAbsent(direction, x -> new HashMap<>());
  }
}
