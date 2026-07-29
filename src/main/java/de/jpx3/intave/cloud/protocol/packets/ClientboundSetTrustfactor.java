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
import de.jpx3.intave.access.player.trust.TrustFactor;
import de.jpx3.intave.cloud.protocol.Direction;
import de.jpx3.intave.cloud.protocol.JsonPacket;
import de.jpx3.intave.cloud.protocol.listener.Clientbound;

public final class ClientboundSetTrustfactor extends JsonPacket<Clientbound> {
  private long id;
  private TrustFactor trustFactor;

  public ClientboundSetTrustfactor() {
    super(Direction.CLIENTBOUND, "SET_TRUSTFACTOR", "1");
  }

  public ClientboundSetTrustfactor(long id, TrustFactor trustFactor) {
    this();
    this.id = id;
    this.trustFactor = trustFactor;
  }

  @Override
  public void serialize(JsonWriter writer) {
    try {
      writer.beginObject();
      writer.name("id").value(id);
      writer.name("factor").value(trustFactor.name());
      writer.endObject();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to serialize trust-factor packet", e);
    }
  }

  @Override
  public void deserialize(JsonReader reader) {
    try {
      reader.beginObject();
      while (reader.peek() == JsonToken.NAME) {
        switch (reader.nextName()) {
          case "id":
            id = reader.nextLong();
            break;
          case "factor":
            trustFactor = TrustFactor.valueOf(reader.nextString());
            break;
        }
      }
      while (reader.hasNext()) {
        reader.skipValue();
      }
      reader.endObject();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to deserialize trust-factor packet", e);
    }
  }

  public long id() {
    return id;
  }

  public TrustFactor trustFactor() {
    return trustFactor;
  }
}
