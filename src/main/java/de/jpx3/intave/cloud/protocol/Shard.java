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

import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;

public class Shard implements Serializable, Comparable<Shard> {
  private String name;
  private String domain;
  private int port;
  private Token token;

  private Shard() {
  }

  public Shard(String name, String domain, int port, Token token) {
    this.name = name;
    this.domain = domain;
    this.port = port;
    this.token = token;
  }

  public String name() {
    return name;
  }

  public String domain() {
    return domain;
  }

  public int port() {
    return port;
  }

  public Token token() {
    return token;
  }

  @Override
  public void serialize(DataOutput buffer) {
    try {
      buffer.writeUTF(name);
      buffer.writeUTF(domain);
      buffer.writeInt(port);
      token.serialize(buffer);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deserialize(DataInput buffer) {
    try {
      name = buffer.readUTF();
      domain = buffer.readUTF();
      port = buffer.readInt();
      token = Token.from(buffer);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Shard) {
      Shard other = (Shard) obj;
      return name.equals(other.name) && domain.equals(other.domain) && port == other.port;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return name.hashCode() ^ domain.hashCode() ^ port; //^ token.hashCode();
  }

  @Override
  public int compareTo(@NotNull Shard o) {
    return token.compareTo(o.token);
  }

  @Override
  public String toString() {
    return "Shard{" + name + "@" + domain + ":" + port + "}";
  }

  public static Shard from(DataInput buffer) {
    Shard shard = new Shard();
    shard.deserialize(buffer);
    return shard;
  }
}
