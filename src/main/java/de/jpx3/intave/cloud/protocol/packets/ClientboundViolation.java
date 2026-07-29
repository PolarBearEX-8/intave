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
import de.jpx3.intave.cloud.protocol.JsonPacket;
import de.jpx3.intave.cloud.protocol.listener.Clientbound;

import static com.google.gson.stream.JsonToken.NAME;
import static de.jpx3.intave.cloud.protocol.Direction.CLIENTBOUND;

public final class ClientboundViolation extends JsonPacket<Clientbound> {
  private long id;
  private String check;
  private String threshold;
  private String message;
  private String details;
  private int vl;

  public ClientboundViolation() {
    super(CLIENTBOUND, "VIOLATION", "1");
  }

  public ClientboundViolation(long id, String check, String threshold, String message, String details, int vl) {
    super(CLIENTBOUND, "VIOLATION", "1");
    this.id = id;
    this.check = check;
    this.threshold = threshold;
    this.message = message;
    this.details = details;
    this.vl = vl;
  }

  @Override
  public void serialize(JsonWriter writer) {
    try {
      writer.beginObject();
      writer.name("id").value(id);
      writer.name("check").value(check);
      writer.name("threshold").value(threshold);
      writer.name("message").value(message);
      writer.name("details").value(details);
      writer.name("vl").value(vl);

      writer.endObject();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to serialize violation packet", e);
    }
  }

  @Override
  public void deserialize(JsonReader reader) {
    try {
      reader.beginObject();

      while (reader.hasNext()) {
        while (reader.peek() == NAME) {
          switch (reader.nextName()) {
            case "id":
              id = reader.nextLong();
              break;
            case "check":
              check = reader.nextString();
              break;
            case "threshold":
              threshold = reader.nextString();
              break;
            case "message":
              message = reader.nextString();
              break;
            case "details":
              details = reader.nextString();
              break;
            case "vl":
              vl = reader.nextInt();
              break;
          }
        }
        if (reader.hasNext()) {
          reader.skipValue();
        }
      }
      reader.endObject();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to deserialize violation packet", e);
    }
  }

  public long id() {
    return id;
  }

  public String check() {
    return check;
  }

  public String threshold() {
    return threshold;
  }

  public String message() {
    return message;
  }

  public String details() {
    return details;
  }

  public int vl() {
    return vl;
  }
}
