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
import de.jpx3.intave.cloud.protocol.Direction;
import de.jpx3.intave.cloud.protocol.JsonPacket;
import de.jpx3.intave.cloud.protocol.listener.Clientbound;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ClientboundInquiryResponse extends JsonPacket<Clientbound> {
  private UUID requestId;
  private Map<String, String> response = new LinkedHashMap<>();

  public ClientboundInquiryResponse() {
    super(Direction.CLIENTBOUND, "INQUIRY_RESPONSE", "1");
  }

  public ClientboundInquiryResponse(UUID requestId, Map<String, String> response) {
    this();
    this.requestId = requestId;
    this.response = response;
  }

  @Override
  public void serialize(JsonWriter writer) {
    try {
      writer.beginObject();
      writer.name("requestId").value(requestId.toString());
      writer.name("response");
      writer.beginObject();
      for (Map.Entry<String, String> entry : response.entrySet()) {
        writer.name(entry.getKey()).value(entry.getValue());
      }
      writer.endObject();
      writer.endObject();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void deserialize(JsonReader reader) {
    response = new LinkedHashMap<>();
    try {
      reader.beginObject();
      while (reader.hasNext()) {
        while (reader.peek() == JsonToken.NAME) {
          switch (reader.nextName()) {
            case "requestId":
              requestId = UUID.fromString(reader.nextString());
              break;
            case "response":
              reader.beginObject();
              while (reader.hasNext()) {
                response.put(reader.nextName(), reader.nextString());
              }
              reader.endObject();
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

  public UUID requestId() {
    return requestId;
  }

  public Map<String, String> response() {
    return response;
  }
}
