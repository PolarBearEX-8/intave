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

package de.jpx3.intave.cloud.protocol.pipeline;

import de.jpx3.intave.cloud.protocol.Direction;
import de.jpx3.intave.cloud.protocol.Packet;
import de.jpx3.intave.cloud.protocol.ProtocolSpecification;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class PacketCodec extends ByteToMessageCodec<Packet<?>> {
	private static final int MAX_PACKET_NAME_BYTES = 256;

	private final ProtocolSpecification protocol;
	private final Direction receiving;
	private final Direction sending;

	public PacketCodec(ProtocolSpecification protocol, Direction receiving) {
		this.protocol = protocol;
		this.receiving = receiving;
		this.sending = receiving.opposite();
	}

	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, ByteBuf byteBuf) {
		try {
			if (packet.direction() != sending) {
				throw new IllegalArgumentException("Packet direction is " + packet.direction() + ", expected " + sending);
			}
			if (protocol.packetIdsKnownFor(sending)) {
				int id = protocol.packetId(sending, packet.name());
				if (id < 0 || id >= 0xFF) {
					throw new IllegalStateException("Negotiated packet id " + id + " is outside the allowed range 0-254");
				}
				byteBuf.writeByte(id);
			} else {
				byteBuf.writeByte(0xFF);
				writeString(packet.name(), byteBuf);
			}
			packet.serialize(new ByteBufOutputStream(byteBuf));
			byteBuf.writeByte(-1);
		} catch (Exception exception) {
			throw new EncoderException("Failed to encode " + sending.name().toLowerCase() + " cloud packet '" + packet.name() + "' (version " + packet.version() + ")", exception);
		}
	}

	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
		int packetStart = byteBuf.readerIndex();
		String packetReference = "unknown packet";
		try {
			int id = byteBuf.readUnsignedByte();
			Packet<?> packet;
			if (id == 0xFF) {
				String name = readString(byteBuf);
				packetReference = "packet '" + name + "'";
				packet = protocol.packetFromName(receiving, name);
			} else {
				packet = protocol.packetFromId(receiving, id);
				packetReference = "packet '" + packet.name() + "' (id " + id + ")";
			}
			if (packet.direction() != receiving) {
				throw new IllegalArgumentException("Packet direction is " + packet.direction() + ", expected " + receiving);
			}
			packet.deserialize(new ByteBufInputStream(byteBuf));
			int trailingMarker = byteBuf.readByte();
			if (trailingMarker != -1) {
				throw new IllegalStateException("Packet payload was not fully consumed; expected trailing marker -1 but got " + trailingMarker);
			}
			list.add(packet);
		} catch (Exception exception) {
			throw new DecoderException("Failed to decode " + receiving.name().toLowerCase() + " " + packetReference + " from " + (byteBuf.writerIndex() - packetStart) + " byte(s)", exception);
		}
	}

	private void writeString(String string, ByteBuf byteBuf) {
		byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
		if (bytes.length > MAX_PACKET_NAME_BYTES) {
			throw new IllegalArgumentException("Packet name is " + bytes.length + " bytes; maximum is " + MAX_PACKET_NAME_BYTES);
		}
		writeVarInt(byteBuf, bytes.length);
		byteBuf.writeBytes(bytes);
	}

	private String readString(ByteBuf byteBuf) {
		int length = readVarInt(byteBuf);
		if (length < 0 || length > MAX_PACKET_NAME_BYTES) {
			throw new DecoderException("Packet name length " + length + " is outside the allowed range 0-" + MAX_PACKET_NAME_BYTES);
		}
		if (length > byteBuf.readableBytes()) {
			throw new DecoderException("Packet name declares " + length + " byte(s), but only " + byteBuf.readableBytes() + " remain");
		}
		byte[] bytes = new byte[length];
		byteBuf.readBytes(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	private int readVarInt(ByteBuf in) {
		int i = 0;
		int bytePosition = 0;
		while (true) {
			int nextByte = in.readByte();
			i |= (nextByte & 0b1111111) << bytePosition++ * 7;
			if (bytePosition > 5) {
				throw new RuntimeException("VarInt too big");
			}
			if ((nextByte & 0b10000000) != 0b10000000) {
				break;
			}
		}
		return i;
	}

	private void writeVarInt(ByteBuf out, int paramInt) {
		while (true) {
			if ((paramInt & 0xFFFFFF80) == 0) {
				out.writeByte(paramInt);
				return;
			}
			out.writeByte(paramInt & 0x7F | 0x80);
			paramInt >>>= 7;
		}
	}
}
