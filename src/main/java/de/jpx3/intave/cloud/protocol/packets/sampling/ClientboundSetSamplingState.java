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

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.jpx3.intave.cloud.protocol.Direction;
import de.jpx3.intave.cloud.protocol.JsonPacket;
import de.jpx3.intave.cloud.protocol.listener.Clientbound;

import java.io.IOException;
import java.util.UUID;

public final class ClientboundSetSamplingState extends JsonPacket<Clientbound> {
	private long id;
	private SamplingState newState;
	private UUID transmissionId;

	public ClientboundSetSamplingState() {
		super(Direction.CLIENTBOUND, "SET_SAMPLING_STATE", "1");
	}

	public ClientboundSetSamplingState(UUID transmissionId) {
		this();
		this.transmissionId = transmissionId;
	}

	public ClientboundSetSamplingState(long id, SamplingState newState, UUID transmissionId) {
		this(transmissionId);
		this.id = id;
		this.newState = newState;
	}

	@Override
	public void serialize(JsonWriter writer) {
		try {
			writer.beginObject();
			writer.name("id").value(id);
			writer.name("state").value(newState.name());
			writer.name("transmissionId").value(transmissionId.toString());
			writer.endObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void deserialize(JsonReader reader) {
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				switch (reader.nextName()) {
					case "id":
						id = reader.nextLong();
						break;
					case "state":
						newState = SamplingState.valueOf(reader.nextString());
						break;
					case "transmissionId":
						transmissionId = UUID.fromString(reader.nextString());
						break;
					default:
						reader.skipValue();
						break;
				}
			}
			reader.endObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public long id() {
		return id;
	}

	public UUID transmissionId() {
		return transmissionId;
	}

	public SamplingState newState() {
		return newState;
	}

	public enum SamplingState {
		START, STOP
	}
}
