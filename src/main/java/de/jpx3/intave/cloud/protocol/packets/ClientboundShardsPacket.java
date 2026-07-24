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

package de.jpx3.intave.cloud.protocol.packets;

import de.jpx3.intave.cloud.protocol.BinaryPacket;
import de.jpx3.intave.cloud.protocol.Shard;
import de.jpx3.intave.cloud.protocol.listener.Clientbound;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.List;

import static de.jpx3.intave.cloud.protocol.Direction.CLIENTBOUND;

public final class ClientboundShardsPacket extends BinaryPacket<Clientbound> {
  private final List<Shard> shards = new ArrayList<>();

  public ClientboundShardsPacket() {
    super(CLIENTBOUND, "SHARDS", "1");
  }

  @Override
  public void serialize(DataOutput buffer) {
    try {
      buffer.writeInt(shards.size());
      for (Shard shard : shards) {
        shard.serialize(buffer);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deserialize(DataInput buffer) {
    try {
      int shardCount = buffer.readInt();
      if (shardCount > 128) {
        throw new RuntimeException("Too many shards");
      }
      for (int i = 0; i < shardCount; i++) {
        shards.add(Shard.from(buffer));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<Shard> shards() {
    return shards;
  }
}
