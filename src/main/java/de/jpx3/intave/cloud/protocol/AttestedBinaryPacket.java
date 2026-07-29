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

import de.jpx3.intave.cloud.protocol.listener.PacketListener;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

public abstract class AttestedBinaryPacket<LISTENER extends PacketListener> extends AttestedPacket<LISTENER> {
	public AttestedBinaryPacket(Direction direction, String name, String version) {
		super(direction, name, version, TransferMode.BINARY);
	}

	public abstract void serializeAttested(DataOutput buffer);

	public abstract void deserializeAttested(DataInput buffer);

	@Override
	public final void serialize(DataOutput buffer) {
		try {
			buffer.writeLong(requestId().getMostSignificantBits());
			buffer.writeLong(requestId().getLeastSignificantBits());
			buffer.writeLong(idempotencyToken().getMostSignificantBits());
			buffer.writeLong(idempotencyToken().getLeastSignificantBits());
			serializeAttested(buffer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public final void deserialize(DataInput buffer) {
		try {
			long requestIdMost = buffer.readLong();
			long requestIdLeast = buffer.readLong();
			long idempotencyTokenMost = buffer.readLong();
			long idempotencyTokenLeast = buffer.readLong();
			UUID readRequestId = new UUID(requestIdMost, requestIdLeast);
			UUID readIdempotencyToken = new UUID(idempotencyTokenMost, idempotencyTokenLeast);
			setIdempotencyToken(readIdempotencyToken);
			setRequestId(readRequestId);
			deserializeAttested(buffer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
