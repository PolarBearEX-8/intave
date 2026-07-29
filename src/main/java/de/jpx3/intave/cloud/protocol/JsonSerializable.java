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

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;

public interface JsonSerializable extends Serializable {
  void serialize(JsonWriter writer);

  void deserialize(JsonReader reader);

  default void serialize(DataOutput output) {
    StringWriter jsonString = new StringWriter();
    JsonWriter writer = new JsonWriter(new BufferedWriter(jsonString));
    try {
      serialize(writer);
      writer.close();
      output.writeUTF(jsonString.toString());
    } catch (IOException e) {
      throw new UncheckedIOException("Unable to serialize JSON packet payload", e);
    }
  }

  default void deserialize(DataInput input) {
    try {
      deserialize(new JsonReader(new StringReader(input.readUTF())));
    } catch (IOException e) {
      throw new UncheckedIOException("Unable to deserialize JSON packet payload", e);
    }
  }
}
