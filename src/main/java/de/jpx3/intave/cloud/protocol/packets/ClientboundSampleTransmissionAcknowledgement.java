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

package de.jpx3.intave.cloud.protocol.packets;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.jpx3.intave.cloud.protocol.Direction;
import de.jpx3.intave.cloud.protocol.Identity;
import de.jpx3.intave.cloud.protocol.JsonPacket;
import de.jpx3.intave.cloud.protocol.listener.Clientbound;

public final class ClientboundSampleTransmissionAcknowledgement extends JsonPacket<Clientbound> {
	private Identity identity;
	private AcceptedState state = AcceptedState.ACCEPTED;

	public ClientboundSampleTransmissionAcknowledgement() {
		super(Direction.CLIENTBOUND, "SAMPLE_TRANSMISSION_ACKNOWLEDGEMENT", "1");
	}

	public Identity identity() {
		return identity;
	}

	public AcceptedState state() {
		return state;
	}

	@Override
	public void serialize(JsonWriter writer) {
		try {
			writer.beginObject();
			writer.name("id");
			identity.serialize(writer);
			writer.name("state");
			writer.value(state.name());
			writer.endObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deserialize(JsonReader reader) {
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				while (reader.peek() == JsonToken.NAME) {
					switch (reader.nextName()) {
						case "id":
							identity = Identity.from(reader);
							break;
						case "state":
							state = AcceptedState.valueOf(reader.nextString());
							break;
					}
				}
				if (reader.hasNext()) {
					reader.skipValue();
				}
			}
			reader.endObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public enum AcceptedState {
		ACCEPTED,
		REJECTED
	}

	public enum Classification {
		LEGIT, CHEAT,
		UNKNOWN
	}
}
