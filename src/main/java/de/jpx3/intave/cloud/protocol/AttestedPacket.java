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

import java.util.UUID;

public abstract class AttestedPacket<LISTENER extends PacketListener> extends Packet<LISTENER> {
	private UUID idempotencyToken;
	private UUID requestId;

	public AttestedPacket(Direction direction, String name, String version, TransferMode transferMode) {
		super(direction, name, version, transferMode);
	}

	public UUID idempotencyToken() {
		return idempotencyToken;
	}

	public UUID requestId() {
		return requestId;
	}

	public void setIdempotencyToken(UUID idempotencyToken) {
		this.idempotencyToken = idempotencyToken;
	}

	public void setRequestId(UUID requestId) {
		this.requestId = requestId;
	}

	public boolean hasIdempotencyToken() {
		return idempotencyToken != null;
	}
}
