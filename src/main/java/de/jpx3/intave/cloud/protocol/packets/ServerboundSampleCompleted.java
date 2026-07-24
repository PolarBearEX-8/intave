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

public final class ServerboundSampleCompleted extends JsonPacket<Serverbound> {
  private Identity identity;

  public ServerboundSampleCompleted() {
    super(Direction.SERVERBOUND,"SAMPLE_COMPLETED", "1");
  }

  public ServerboundSampleCompleted(Identity identity) {
    this();
    this.identity = identity;
  }

  @Override
  public void serialize(JsonWriter writer) {
    try {
      writer.beginObject();
      writer.name("id");
      identity.serialize(writer);
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
        switch (reader.nextName()) {
          case "id":
            identity = Identity.from(reader);
            break;
        }
      }
      reader.endObject();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
