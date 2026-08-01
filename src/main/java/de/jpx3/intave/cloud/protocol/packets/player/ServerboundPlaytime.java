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

public final class ServerboundPlaytime extends AttestedJsonPacket<Serverbound> {
	private long playerId;
	private long activeTicks;
	private long passiveTicks;

	public ServerboundPlaytime() {
		super(Direction.SERVERBOUND, "PLAYTIME", "1");
	}

	public ServerboundPlaytime(long playerId, long activeTicks, long passiveTicks) {
		this();
		this.playerId = playerId;
		this.activeTicks = activeTicks;
		this.passiveTicks = passiveTicks;
	}

	@Override
	public void serializeAttested(JsonWriter writer) {
		try {
			writer.beginObject();
			writer.name("playerId").value(playerId);
			writer.name("activeTicks").value(activeTicks);
			writer.name("passiveTicks").value(passiveTicks);
			writer.endObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void deserializeAttested(JsonReader reader) {
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				switch (name) {
					case "playerId":
						playerId = reader.nextLong();
						break;
					case "activeTicks":
						activeTicks = reader.nextLong();
						break;
					case "passiveTicks":
						passiveTicks = reader.nextLong();
						break;
					default:
						throw new RuntimeException("Unexpected field: " + name);
				}
			}
			reader.endObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
