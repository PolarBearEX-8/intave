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
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.annotate.Nullable;
import de.jpx3.intave.user.User;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

public final class Identity implements JsonSerializable {
  @Nullable
  private UUID uuid;
  @Nullable
  private String name;
  @Nullable
  private InetAddress netaddr;

  private Identity() {
  }

  public Identity(String name) {
    this.name = name;
  }

  public Identity(UUID id) {
    this.uuid = id;
  }

  public Identity(UUID id, String name) {
    this.uuid = id;
    this.name = name;
  }

  public Identity(UUID id, String name, InetAddress netaddr) {
    this.uuid = id;
    this.name = name;
    this.netaddr = netaddr;
  }

  public UUID id() {
    return uuid;
  }

  public String name() {
    return name;
  }

  public InetAddress address() {
    return netaddr;
  }

  @Override
  public void serialize(JsonWriter writer) {
    try {
      writer.beginObject();
      if (uuid != null) {
        writer.name("uuid").value(uuid.toString());
      }
      if (name != null) {
        writer.name("name").value(name);
      }
      if (netaddr != null) {
        writer.name("netaddr").value(netaddr.getHostAddress());
      }
      writer.endObject();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to serialize cloud identity as JSON", e);
    }
  }

  @Override
  public void deserialize(JsonReader reader) {
    try {
      reader.beginObject();
      while (reader.hasNext()) {
        while (reader.peek() == JsonToken.NAME) {
          switch (reader.nextName()) {
            case "uuid":
              uuid = UUID.fromString(reader.nextString());
              break;
            case "name":
              name = reader.nextString();
              break;
            case "netaddr":
              netaddr = InetAddress.getByName(reader.nextString());
              break;
            default:
              reader.skipValue();
              break;
          }
        }
        if (reader.hasNext()) {
          reader.skipValue();
        }
      }
      reader.endObject();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to deserialize cloud identity from JSON", e);
    }
  }

  @Override
  public void serialize(DataOutput buffer) {
    try {
      buffer.writeBoolean(uuid != null);
      if (uuid != null) {
        buffer.writeLong(uuid.getMostSignificantBits());
        buffer.writeLong(uuid.getLeastSignificantBits());
      }
      buffer.writeBoolean(name != null);
      if (name != null) {
        buffer.writeUTF(name);
      }
      buffer.writeBoolean(netaddr != null);
      if (netaddr != null) {
        buffer.writeUTF(netaddr.getHostAddress());
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unable to serialize cloud identity", e);
    }
  }

  @Override
  public void deserialize(DataInput buffer) {
    try {
      if (buffer.readBoolean()) {
        uuid = new UUID(buffer.readLong(), buffer.readLong());
      }
      if (buffer.readBoolean()) {
        name = buffer.readUTF();
      }
      if (buffer.readBoolean()) {
        netaddr = InetAddress.getByName(buffer.readUTF());
      }
      if (uuid == null && name == null) {
        throw new IOException("Identity is empty");
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unable to deserialize cloud identity", e);
    }
  }

  public Player find() {
    Server server = IntavePlugin.singletonInstance().getServer();
    if (uuid != null) {
      Player player = server.getPlayer(uuid);
      if (player != null) {
        return player;
      }
    }
    if (name != null) {
	    return server.getPlayerExact(name);
    }
    return null;
  }

  public static Identity from(String name) {
    return new Identity(name);
  }

  public static Identity from(Player player) {
    InetAddress inetAddress = null;
    if (IntavePlugin.singletonInstance().cloud().config().privacy().annotateINetAdds()) {
      inetAddress = player.getAddress().getAddress();
    }
    return new Identity(player.getUniqueId(), player.getName(), inetAddress);
  }

  public static Identity from(User user) {
    if (!user.hasPlayer()) {
      throw new IllegalArgumentException("User does not have a player");
    }
    return from(user.player());
  }

  public static Identity from(UUID id) {
    return new Identity(id);
  }

  public static Identity from(JsonReader reader) {
    Identity identity = new Identity();
    identity.deserialize(reader);
    return identity;
  }

  public static Identity from(DataInput buffer) {
    Identity identity = new Identity();
    identity.deserialize(buffer);
    return identity;
  }
}
