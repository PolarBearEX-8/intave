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
import de.jpx3.intave.cloud.protocol.listener.Serverbound;

import java.io.IOException;

public final class ServerboundPlayerLogout extends AttestedJsonPacket<Serverbound> {
	private long id;

	public ServerboundPlayerLogout() {
		super(Direction.SERVERBOUND, "PLAYER_LOGOUT", "1");
	}

	public ServerboundPlayerLogout(long id) {
		this();
		this.id = id;
	}

	@Override
	public void serializeAttested(JsonWriter writer) {
		try {
			writer.beginObject();
			writer.name("id").value(id);
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
					case "id":
						id = reader.nextLong();
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

	public long id() {
		return id;
	}
}
