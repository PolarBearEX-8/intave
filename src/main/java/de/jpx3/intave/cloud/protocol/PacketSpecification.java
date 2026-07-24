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

import java.io.DataInput;
import java.io.DataOutput;

public class PacketSpecification implements Serializable {
  private String version;
  private TransferMode transferMode;

  public PacketSpecification() {
  }

  public PacketSpecification(String version, TransferMode transferMode) {
    this.version = version;
    this.transferMode = transferMode;
  }

  @Override
  public void serialize(DataOutput buffer) {
    try {
      buffer.writeUTF(version);
      buffer.writeUTF(transferMode.name());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deserialize(DataInput buffer) {
    try {
      version = buffer.readUTF();
      transferMode = TransferMode.valueOf(buffer.readUTF());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static PacketSpecification from(DataInput buffer) {
    PacketSpecification packetSpecification = new PacketSpecification();
    packetSpecification.deserialize(buffer);
    return packetSpecification;
  }

  public static PacketSpecification from(Packet<?> packet) {
    return new PacketSpecification(packet.version(), packet.transferMode());
  }
}
