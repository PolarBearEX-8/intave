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
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ClientboundSendMessage extends JsonPacket<Clientbound> {
	private long playerId;
	private final List<TextComponent> lines = new ArrayList<>();

	public ClientboundSendMessage() {
		super(Direction.CLIENTBOUND, "PLAYER_CHAT", "1");
	}

	public ClientboundSendMessage(long playerId, TextComponent... components) {
		this();
		this.playerId = playerId;
		this.lines.addAll(Arrays.asList(components));
	}

	@Override
	public void serialize(JsonWriter writer) {
		try {
			writer.beginObject();
			writer.name("playerId").value(playerId);
			writer.name("lines");
			writer.beginArray();
			for (TextComponent component : lines) {
				writer.value(ComponentSerializer.toString(component));
			}
			writer.endArray();
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
					case "playerId":
						playerId = reader.nextLong();
						break;
					case "lines":
						reader.beginArray();
						while (reader.hasNext()) {
							String line = reader.nextString();
							BaseComponent baseComponent = ComponentSerializer.parse(line)[0];
							if (baseComponent instanceof TextComponent) {
								TextComponent component = (TextComponent) baseComponent;
								lines.add(component);
							} else {
								throw new IllegalStateException("Expected TextComponent but got " + baseComponent.getClass().getSimpleName());
							}
						}
						reader.endArray();
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

	public long playerId() {
		return playerId;
	}

	public List<TextComponent> lines() {
		return lines;
	}
}
