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

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.jpx3.intave.cloud.protocol.Direction;
import de.jpx3.intave.cloud.protocol.Identity;
import de.jpx3.intave.cloud.protocol.JsonPacket;
import de.jpx3.intave.cloud.protocol.listener.Serverbound;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ServerboundPlayerPlayStateChange extends JsonPacket<Serverbound> {
  private Identity identity;
  private PlayState state;
  private UUID gameId;
  private final List<Identity> interacted = new ArrayList<>();

  public ServerboundPlayerPlayStateChange() {
    super(Direction.SERVERBOUND, "PLAYER_PLAY_STATE_CHANGE", "1");
  }

  public ServerboundPlayerPlayStateChange(Identity identity, PlayState state, UUID gameId, List<Identity> interacted) {
    this();
    this.identity = identity;
    this.state = state;
    this.gameId = gameId;
    this.interacted.addAll(interacted);
  }

  @Override
  public void serialize(JsonWriter writer) {
    try {
      writer.beginObject();
      writer.name("identity");
      identity.serialize(writer);
      writer.name("state");
      writer.value(state.name());
      writer.name("gameId");
      writer.value(gameId.toString());
      writer.name("interacted");
      writer.beginArray();
      for (Identity identity : interacted) {
        identity.serialize(writer);
      }
      writer.endArray();
      writer.endObject();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void deserialize(JsonReader reader) {
    try {
      reader.beginObject();
      while (reader.hasNext()) {
        while (reader.peek() == com.google.gson.stream.JsonToken.NAME) {
          switch (reader.nextName()) {
            case "identity":
              identity = Identity.from(reader);
              break;
            case "state":
              state = PlayState.valueOf(reader.nextString());
              break;
            case "gameId":
              gameId = UUID.fromString(reader.nextString());
              break;
            case "interacted":
              reader.beginArray();
              while (reader.hasNext()) {
                interacted.add(Identity.from(reader));
              }
              reader.endArray();
              break;
          }
        }
        if (reader.hasNext()) {
          reader.skipValue();
        }
      }
      reader.endObject();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Identity identity() {
    return identity;
  }

  public PlayState state() {
    return state;
  }

  public UUID gameId() {
    return gameId;
  }

  public List<Identity> interacted() {
    return interacted;
  }

  public static ServerboundPlayerPlayStateChange from(
    Identity identity, PlayState state,
    UUID gameId, List<Identity> interacted
  ) {
    return new ServerboundPlayerPlayStateChange(identity, state, gameId, interacted);
  }

  public enum PlayState {
    JOIN, LEAVE
  }
}
