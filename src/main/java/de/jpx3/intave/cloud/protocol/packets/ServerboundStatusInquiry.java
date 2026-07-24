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
import de.jpx3.intave.annotate.Nullable;
import de.jpx3.intave.cloud.protocol.Direction;
import de.jpx3.intave.cloud.protocol.Identity;
import de.jpx3.intave.cloud.protocol.JsonPacket;
import de.jpx3.intave.cloud.protocol.listener.Serverbound;

import java.util.UUID;

public class ServerboundStatusInquiry extends JsonPacket<Serverbound> {
  private Type type;
  @Nullable
  private Identity identity;
  private UUID requestId;

  public ServerboundStatusInquiry() {
    super(Direction.SERVERBOUND, "STATUS_INQUIRY", "1");
  }

  public ServerboundStatusInquiry(UUID requestId, Type type, Identity identity) {
    this();
    this.type = type;
    this.identity = identity;
    this.requestId = requestId;
  }

  @Override
  public void serialize(JsonWriter writer) {
    try {
      writer.beginObject();
      writer.name("type").value(type.name());
      if (identity != null) {
        writer.name("id");
        identity.serialize(writer);
      }
      writer.name("requestId").value(requestId.toString());
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
            case "type":
              type = Type.valueOf(reader.nextString());
              break;
            case "id":
              identity = Identity.from(reader);
              break;
            case "requestId":
              requestId = UUID.fromString(reader.nextString());
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

  public enum Type {
    GENERAL,
    PLAYER
  }
}
