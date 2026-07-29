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
import de.jpx3.intave.cloud.protocol.Direction;
import de.jpx3.intave.cloud.protocol.JsonPacket;
import de.jpx3.intave.cloud.protocol.listener.Clientbound;

import java.io.IOException;

public final class ClientboundClarifyUnknownPlayerId extends JsonPacket<Clientbound> {
	private long playerId = -1;

	public ClientboundClarifyUnknownPlayerId() {
		super(Direction.CLIENTBOUND, "CLARIFY_UNKNOWN_PLAYER_ID", "1");
	}

	public ClientboundClarifyUnknownPlayerId(long playerId) {
		this();
		this.playerId = playerId;
	}

	@Override
	public void serialize(JsonWriter writer) {
		try {
			writer.beginObject();
			writer.name("playerid").value(playerId);
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
					case "playerid":
						playerId = reader.nextLong();
						break;
					default:
						reader.skipValue();
				}
			}
			reader.endObject();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	public long playerId() {
		return playerId;
	}
}
