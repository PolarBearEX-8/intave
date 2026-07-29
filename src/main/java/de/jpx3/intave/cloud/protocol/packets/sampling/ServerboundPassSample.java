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

package de.jpx3.intave.cloud.protocol.packets.sampling;

import de.jpx3.intave.cloud.protocol.BinaryPacket;
import de.jpx3.intave.cloud.protocol.listener.Serverbound;

import java.io.DataInput;
import java.io.DataOutput;
import java.nio.ByteBuffer;
import java.util.UUID;

import static de.jpx3.intave.cloud.protocol.Direction.SERVERBOUND;

public final class ServerboundPassSample extends BinaryPacket<Serverbound> {
  private long id;
  private UUID transmissionId;
  private long sampleSubIndex;
  private ByteBuffer data;

  public ServerboundPassSample() {
    super(SERVERBOUND, "PASS_SAMPLE", "1");
  }

  public ServerboundPassSample(
    long id, UUID transmissionId,
    long sampleSubIndex, ByteBuffer data
  ) {
    super(SERVERBOUND, "PASS_SAMPLE", "1");
    this.id = id;
    this.transmissionId = transmissionId;
    this.sampleSubIndex = sampleSubIndex;
    this.data = data;
  }

  @Override
  public void serialize(DataOutput buffer) {
    try {
      buffer.writeLong(id);
      buffer.writeUTF(transmissionId.toString());
      buffer.writeLong(sampleSubIndex);
      byte[] array = data.array();
      buffer.writeInt(array.length);
      buffer.write(array);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to serialize sample packet", e);
    }
  }

  @Override
  public void deserialize(DataInput buffer) {
    try {
      id = buffer.readLong();
      transmissionId = UUID.fromString(buffer.readUTF());
      sampleSubIndex = buffer.readLong();
      int size = buffer.readInt();
      if (size > 1024 * 1024 * 50) {
        throw new RuntimeException("Too big");
      }
      byte[] array = new byte[size];
      buffer.readFully(array);
      data = ByteBuffer.wrap(array);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to deserialize sample packet", e);
    }
  }

  public long id() {
    return id;
  }

  public UUID transmissionId() {
    return transmissionId;
  }

  public long sampleSubIndex() {
    return sampleSubIndex;
  }

  public ByteBuffer data() {
    return data;
  }
}
