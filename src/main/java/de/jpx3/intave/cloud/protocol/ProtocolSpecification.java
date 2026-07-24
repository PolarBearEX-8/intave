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
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ProtocolSpecification {
  private final Map<Direction, Set<String>> packetsNames = Maps.newEnumMap(Direction.class);
  private final Map<Direction, Map<Integer, String>> packetIdsToName = Maps.newEnumMap(Direction.class);
  private final Map<Direction, Map<String, Integer>> packetNamesToId = Maps.newEnumMap(Direction.class);
  private final Map<Direction, Boolean> idsKnown = Maps.newEnumMap(Direction.class);

  public ProtocolSpecification() {
    packetsNames.put(Direction.CLIENTBOUND, Sets.newHashSet("HELLO", "DISCONNECT"));
    packetsNames.put(Direction.SERVERBOUND, Sets.newHashSet("HELLO"));
  }

  public Packet<?> packetFromName(Direction direction, String name) {
    return PacketRegistry.fromName(direction, name);
  }

  public Packet<?> packetFromId(Direction direction, int id) {
    return PacketRegistry.fromAssignedId(this, direction, id);
  }

  public void overrideAvailablePackets(Direction direction, Set<String> packetNames) {
    packetsNames.put(direction, packetNames);
  }

  public void overridePacketIds(Direction direction, List<String> packetNames) {
    Map<Integer, String> idToName = new HashMap<>();
    Map<String, Integer> nameToId = new HashMap<>();
    for (int i = 0; i < packetNames.size(); i++) {
      idToName.put(i, packetNames.get(i));
      nameToId.put(packetNames.get(i), i);
    }
    packetIdsToName.put(direction, idToName);
    packetNamesToId.put(direction, nameToId);
    idsKnown.put(direction, true);
  }

  public Map<Integer, String> packetIdsOf(Direction direction) {
    return packetIdsToName.get(direction);
  }

  public boolean packetIdsKnownFor(Direction direction) {
    return idsKnown.containsKey(direction);
  }

  public boolean packetAvailable(Direction direction, String name) {
    return packetsNames.get(direction).contains(name);
  }

  public int packetId(Direction direction, String name) {
    return packetNamesToId.get(direction).get(name);
  }
}
