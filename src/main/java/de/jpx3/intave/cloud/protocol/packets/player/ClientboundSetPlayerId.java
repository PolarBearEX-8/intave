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

package de.jpx3.intave.cloud.protocol.packets.player;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.jpx3.intave.cloud.protocol.Direction;
import de.jpx3.intave.cloud.protocol.Identity;
import de.jpx3.intave.cloud.protocol.JsonPacket;
import de.jpx3.intave.cloud.protocol.listener.Clientbound;

import java.io.IOException;

public final class ClientboundSetPlayerId extends JsonPacket<Clientbound> {
  private Identity identity;
  private long id;

  public ClientboundSetPlayerId() {
    super(Direction.CLIENTBOUND, "SET_PLAYER_ID", "1");
  }

  public ClientboundSetPlayerId(Identity identity, long id) {
    this();
    this.identity = identity;
    this.id = id;
  }

  @Override
  public void serialize(JsonWriter writer) {
    try {
      writer.beginObject();
      writer.name("identity");
      identity.serialize(writer);
      writer.name("cloudid").value(id);
      writer.endObject();
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  @Override
  public void deserialize(JsonReader reader) {
    try {
      reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "identity":
            identity = Identity.from(reader);
            break;
          case "cloudid":
            id = reader.nextLong();
            break;
          default:
            reader.skipValue();
            break;
        }
      }
      reader.endObject();
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  public Identity identity() {
    return identity;
  }

  public long id() {
    return id;
  }
}
