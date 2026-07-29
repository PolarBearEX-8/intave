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

package de.jpx3.intave.cloud.protocol.packets.base;

import de.jpx3.intave.cloud.protocol.BinaryPacket;
import de.jpx3.intave.cloud.protocol.Direction;
import de.jpx3.intave.cloud.protocol.listener.Clientbound;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ClientboundConfirmAttestations extends BinaryPacket<Clientbound> {
	private List<UUID> receivedRequests = new ArrayList<>();

	public ClientboundConfirmAttestations() {
		super(Direction.CLIENTBOUND, "CONFIRM_ATTESTATIONS", "1");
	}

	@Override
	public void serialize(DataOutput buffer) {
		try {
			buffer.writeInt(receivedRequests.size());
			for (UUID requestId : receivedRequests) {
				buffer.writeLong(requestId.getMostSignificantBits());
				buffer.writeLong(requestId.getLeastSignificantBits());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void deserialize(DataInput buffer) {
		try {
			int size = buffer.readInt();
			if (size < 0) {
				throw new IllegalStateException("Negative request ID count: " + size);
			}
			if (size > 1024) {
				throw new IllegalStateException("Request ID count exceeds maximum: " + size);
			}
			receivedRequests = new ArrayList<>(size);
			for (int i = 0; i < size; i++) {
				long mostSigBits = buffer.readLong();
				long leastSigBits = buffer.readLong();
				receivedRequests.add(new UUID(mostSigBits, leastSigBits));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<UUID> requestIds() {
		return receivedRequests;
	}
}
