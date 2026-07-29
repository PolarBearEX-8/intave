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

import de.jpx3.intave.cloud.protocol.*;
import de.jpx3.intave.cloud.protocol.pipeline.PacketCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public abstract class PacketSerializationTest {
	protected static final UUID PLAYER_UUID = UUID.fromString("12345678-1234-5678-9abc-def012345678");
	protected static final UUID TRANSMISSION_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
	protected static final UUID IDEMPOTENCY_TOKEN = UUID.fromString("00000000-0000-0000-0000-000000000001");
	protected static final UUID REQUEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
	protected static final long KEEP_ALIVE_TIME = 1_723_456_789_012L;

	protected abstract PacketFixture fixture();

	@Test
	public final void serializesAndDeserializesPayload() {
		PacketFixture fixture = fixture();
		Packet<?> packet = decodePayload(fixture);
		assertMetadata(fixture, packet);
		fixture.payloadVerifier.accept(packet);

		byte[] encoded = encodePayload(packet);
		fixture.encodedPayloadVerifier.accept(encoded);
	}

	@Test
	public final void serializesThroughNamedPacketFraming() {
		assertCodecRoundTrip(fixture(), false);
	}

	@Test
	public final void serializesThroughNegotiatedPacketFraming() {
		assertCodecRoundTrip(fixture(), true);
	}

	protected static PacketFixture fixture(Direction direction, String name, String version, TransferMode transferMode, byte[] payload, Consumer<Packet<?>> verifier) {
		return fixture(direction, name, version, transferMode, payload, verifier, verifier, encoded -> assertArrayEquals(payload, encoded));
	}

	protected static PacketFixture fixture(Direction direction, String name, String version, TransferMode transferMode, byte[] payload, Consumer<Packet<?>> payloadVerifier, Consumer<Packet<?>> codecVerifier, Consumer<byte[]> encodedPayloadVerifier) {
		return new PacketFixture(direction, name, version, transferMode, payload, payloadVerifier, codecVerifier, encodedPayloadVerifier);
	}

	protected static byte[] attestedJsonPayload(String content) {
		return jsonPayload("{\"attestation\":{\"idempotencyToken\":\"" + IDEMPOTENCY_TOKEN + "\",\"requestId\":\"" + REQUEST_ID + "\"},\"content\":" + content + "}");
	}

	protected static byte[] jsonPayload(String json) {
		return binaryPayload(output -> output.writeUTF(json));
	}

	protected static String jsonString(String value) {
		try {
			StringWriter string = new StringWriter();
			com.google.gson.stream.JsonWriter writer = new com.google.gson.stream.JsonWriter(string);
			writer.value(value);
			writer.close();
			return string.toString();
		} catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
	}

	protected static byte[] binaryPayload(PayloadWriter writer) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream output = new DataOutputStream(bytes);
			writer.write(output);
			output.flush();
			return bytes.toByteArray();
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to create packet fixture", exception);
		}
	}

	protected static void writeUuid(DataOutputStream output, UUID uuid) throws IOException {
		output.writeLong(uuid.getMostSignificantBits());
		output.writeLong(uuid.getLeastSignificantBits());
	}

	protected static void assertIdentity(Identity identity) {
		assertNotNull(identity);
		assertEquals(PLAYER_UUID, identity.id());
		assertEquals("CloudPlayer", identity.name());
		assertEquals("192.0.2.10", identity.address().getHostAddress());
	}

	protected static void assertAttestation(Packet<?> packet) {
		AttestedPacket<?> attested = (AttestedPacket<?>) packet;
		assertTrue(attested.hasIdempotencyToken());
		assertEquals(IDEMPOTENCY_TOKEN, attested.idempotencyToken());
		assertEquals(REQUEST_ID, attested.requestId());
	}

	private static void assertCodecRoundTrip(PacketFixture fixture, boolean useNegotiatedIds) {
		Packet<?> source = decodePayload(fixture);
		ProtocolSpecification protocol = new ProtocolSpecification();
		if (useNegotiatedIds) {
			protocol.overridePacketIds(fixture.direction, Collections.singletonList(fixture.name));
		}

		EmbeddedChannel encoder = new EmbeddedChannel(new PacketCodec(protocol, fixture.direction.opposite()));
		EmbeddedChannel decoder = new EmbeddedChannel(new PacketCodec(protocol, fixture.direction));
		ByteBuf encoded = null;
		try {
			assertTrue(encoder.writeOutbound(source));
			encoded = encoder.readOutbound();
			assertNotNull(encoded);
			assertTrue(decoder.writeInbound(encoded));

			Packet<?> actual = decoder.readInbound();
			assertNotNull(actual);
			assertMetadata(fixture, actual);
			fixture.codecVerifier.accept(actual);
			assertNull(decoder.readInbound());
		} finally {
			if (encoded != null && encoded.refCnt() > 0) {
				encoded.release();
			}
			encoder.finishAndReleaseAll();
			decoder.finishAndReleaseAll();
		}
	}

	private static void assertMetadata(PacketFixture fixture, Packet<?> packet) {
		assertEquals(fixture.direction, packet.direction());
		assertEquals(fixture.name, packet.name());
		assertEquals(fixture.version, packet.version());
		assertEquals(fixture.transferMode, packet.transferMode());
	}

	private static Packet<?> decodePayload(PacketFixture fixture) {
		Packet<?> packet = PacketRegistry.fromName(fixture.direction, fixture.name);
		packet.deserialize(new DataInputStream(new ByteArrayInputStream(fixture.payload)));
		return packet;
	}

	private static byte[] encodePayload(Packet<?> packet) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		packet.serialize(new DataOutputStream(bytes));
		return bytes.toByteArray();
	}

	@FunctionalInterface
	protected interface PayloadWriter {
		void write(DataOutputStream output) throws Exception;
	}

	protected static final class PacketFixture {
		private final Direction direction;
		private final String name;
		private final String version;
		private final TransferMode transferMode;
		private final byte[] payload;
		private final Consumer<Packet<?>> payloadVerifier;
		private final Consumer<Packet<?>> codecVerifier;
		private final Consumer<byte[]> encodedPayloadVerifier;

		private PacketFixture(Direction direction, String name, String version, TransferMode transferMode, byte[] payload, Consumer<Packet<?>> payloadVerifier, Consumer<Packet<?>> codecVerifier, Consumer<byte[]> encodedPayloadVerifier) {
			this.direction = direction;
			this.name = name;
			this.version = version;
			this.transferMode = transferMode;
			this.payload = payload;
			this.payloadVerifier = payloadVerifier;
			this.codecVerifier = codecVerifier;
			this.encodedPayloadVerifier = encodedPayloadVerifier;
		}
	}
}
