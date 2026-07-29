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

package de.jpx3.intave.cloud.protocol.packets.player;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.jpx3.intave.cloud.protocol.AttestedJsonPacket;
import de.jpx3.intave.cloud.protocol.Direction;
import de.jpx3.intave.cloud.protocol.Identity;
import de.jpx3.intave.cloud.protocol.listener.Serverbound;

import java.io.IOException;

public final class ServerboundPlayerLogin extends AttestedJsonPacket<Serverbound> {
	private Identity identity;
	private long requestedId;

	public ServerboundPlayerLogin() {
		super(Direction.SERVERBOUND, "PLAYER_LOGIN", "1");
	}

	public ServerboundPlayerLogin(Identity identity) {
		this(identity, -1);
	}

	public ServerboundPlayerLogin(Identity identity, long requestedId) {
		this();
		this.identity = identity;
		this.requestedId = requestedId;
	}

	@Override
	public void serializeAttested(JsonWriter writer) {
		try {
			writer.beginObject();
			writer.name("identity");
			identity.serialize(writer);
			writer.name("requestedId").value(requestedId);
			writer.endObject();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public void deserializeAttested(JsonReader reader) {
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				switch (reader.nextName()) {
					case "identity":
						identity = Identity.from(reader);
						break;
					case "requestedId":
						requestedId = reader.nextLong();
						break;
					default:
						reader.skipValue();
						break;
				}
			}
			reader.endObject();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	public Identity identity() {
		return identity;
	}

	public long requestedId() {
		return requestedId;
	}
}
