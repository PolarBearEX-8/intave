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
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.jpx3.intave.cloud.protocol.Identity;
import de.jpx3.intave.cloud.protocol.JsonPacket;
import de.jpx3.intave.cloud.protocol.listener.Clientbound;

import static de.jpx3.intave.cloud.protocol.Direction.CLIENTBOUND;

public final class ClientboundCombatModifier extends JsonPacket<Clientbound> {
  private Identity id;
  private String modifier;
  private int duration;

  public ClientboundCombatModifier() {
    super(CLIENTBOUND, "REQUEST_ALTERATION", "1");
  }

  @Override
  public void serialize(JsonWriter writer) {
    try {
      writer.beginObject();
      writer.name("id");
      id.serialize(writer);
      writer.name("modifier").value(modifier);
      writer.name("duration").value(duration);
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
        while (reader.peek() == JsonToken.NAME) {
          switch (reader.nextName()) {
            case "id":
              id = Identity.from(reader);
              break;
            case "modifier":
              modifier = reader.nextString();
              break;
            case "duration":
              duration = reader.nextInt();
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
    return id;
  }

  public String modifier() {
    return modifier;
  }

  public int duration() {
    return duration;
  }
}
