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
import de.jpx3.intave.cloud.protocol.listener.PacketListener;

import java.util.UUID;

public abstract class AttestedJsonPacket<LISTENER extends PacketListener> extends AttestedPacket<LISTENER> implements JsonSerializable {
	public AttestedJsonPacket(Direction direction, String name, String version) {
		super(direction, name, version, TransferMode.JSON);
	}

	public abstract void serializeAttested(JsonWriter writer);

	public abstract void deserializeAttested(JsonReader reader);

	@Override
	public final void serialize(JsonWriter writer) {
		try {
			writer.beginObject();
			writer.name("attestation");
				writer.beginObject();
				writer.name("idempotencyToken");
				writer.value(idempotencyToken().toString());
				writer.name("requestId");
				writer.value(requestId().toString());
				writer.endObject();
		  writer.name("content");
				serializeAttested(writer);
			writer.endObject();

		} catch (Exception e) {
			throw new IllegalStateException(
				"Unable to serialize attested cloud packet '" + name() + "'",
				e
			);
		}
	}

	@Override
	public final void deserialize(JsonReader reader) {
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				switch (name) {
					case "attestation":
						reader.beginObject();
						while (reader.hasNext()) {
							String attestationField = reader.nextName();
							switch (attestationField) {
								case "idempotencyToken":
									setIdempotencyToken(UUID.fromString(reader.nextString()));
									break;
								case "requestId":
									setRequestId(UUID.fromString(reader.nextString()));
									break;
								default:
									throw new RuntimeException("Unknown attestation field: " + attestationField);
							}
						}
						reader.endObject();
						break;
					case "content":
						deserializeAttested(reader);
						break;
					default:
						throw new RuntimeException("Unknown field: " + name);
				}
			}
			reader.endObject();
		} catch (Exception e) {
			throw new IllegalStateException(
				"Unable to deserialize attested cloud packet '" + name() + "'",
				e
			);
		}
	}
}
