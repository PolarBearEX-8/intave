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
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public final class Token implements JsonSerializable, Comparable<Token> {
  private byte[] tokenBytes;
  private long validUntil;

  public Token() {
    tokenBytes = new byte[0];
    validUntil = 0;
  }

  public Token(byte[] tokenBytes, long validUntil) {
    this.tokenBytes = tokenBytes;
    this.validUntil = validUntil;
  }

  public byte[] tokenBytes() {
    return tokenBytes;
  }

  public boolean hasExpireInformation() {
    return validUntil != 0;
  }

  public boolean isExpired() {
    return validUntil < System.currentTimeMillis();
  }

  public boolean isStillValidIn(long value, TimeUnit unit) {
    return validUntil > System.currentTimeMillis() + unit.toMillis(value);
  }

  public boolean isLongerValidThan(Token other) {
    return validUntil > other.validUntil;
  }

  @Override
  public void serialize(JsonWriter writer) {
    try {
      writer.beginObject();
      writer.name("content").value(base64Of(tokenBytes));
      writer.name("validUntil").value(validUntil);
      writer.endObject();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static String base64Of(byte[] array) {
    return Base64.getEncoder().encodeToString(array);
  }

  @Override
  public void deserialize(JsonReader reader) {
    try {
      reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "content":
            tokenBytes = bytesOf(reader.nextString());
            break;
          case "validUntil":
            validUntil = reader.nextLong();
            break;
        }
      }
      reader.endObject();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static byte[] bytesOf(String base64) {
    return Base64.getDecoder().decode(base64);
  }

  @Override
  public void serialize(DataOutput buffer) {
    try {
      buffer.writeLong(validUntil);
      buffer.writeInt(tokenBytes.length);
      buffer.write(tokenBytes);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void deserialize(DataInput buffer) {
    try {
      validUntil = buffer.readLong();
      tokenBytes = new byte[buffer.readInt()];
      buffer.readFully(tokenBytes);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Token) {
      Token other = (Token) obj;
      return validUntil == other.validUntil && Arrays.equals(tokenBytes, other.tokenBytes);
    }
    return false;
  }

  @Override
  public int compareTo(@NotNull Token o) {
    return Long.compare(validUntil, o.validUntil);
  }

  public static Token from(DataInput input) {
    Token token = new Token();
    token.deserialize(input);
    return token;
  }

  public static Token from(JsonReader reader) {
    Token token = new Token();
    token.deserialize(reader);
    return token;
  }
}
