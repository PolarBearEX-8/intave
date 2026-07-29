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

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.jpx3.intave.cloud.protocol.AttestedJsonPacket;
import de.jpx3.intave.cloud.protocol.listener.Serverbound;

import static de.jpx3.intave.cloud.protocol.Direction.SERVERBOUND;

public final class ServerboundReport extends AttestedJsonPacket<Serverbound> {
	private long playerId;
	private JsonObject report;

	public ServerboundReport() {
		super(SERVERBOUND, "REPORT", "1");
	}

	public ServerboundReport(long playerId, JsonObject report) {
		super(SERVERBOUND, "REPORT", "1");
		this.playerId = playerId;
		this.report = report;
	}

	@Override
	public void serializeAttested(JsonWriter writer) {
		try {
			writer.beginObject();
			writer.name("playerId").value(playerId);
			writer.name("report");
			Streams.write(report, writer);
			writer.endObject();
		} catch (Exception e) {
			throw new IllegalStateException("Unable to serialize report packet", e);
		}
	}

	@Override
	public void deserializeAttested(JsonReader reader) {
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				switch (reader.nextName()) {
					case "playerId":
						playerId = reader.nextLong();
						break;
					case "report":
						report = Streams.parse(reader).getAsJsonObject();
						break;
				}
			}
			reader.endObject();
		} catch (Exception e) {
			throw new IllegalStateException("Unable to deserialize report packet", e);
		}
	}
}
