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

package de.jpx3.intave.cloud.protocol.packets.base;

import de.jpx3.intave.cloud.protocol.packets.PacketSerializationTest;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.List;

import static de.jpx3.intave.cloud.protocol.Direction.CLIENTBOUND;
import static de.jpx3.intave.cloud.protocol.TransferMode.BINARY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ClientboundHelloTest extends PacketSerializationTest {
  private static final KeyPair KEY_PAIR = rsaKeyPair();

  @Override
  protected PacketFixture fixture() {
    List<String> clientboundPackets =
      Arrays.asList("PLAYER_CHAT", "VIOLATION");
    List<String> serverboundPackets =
      Arrays.asList("REPORT", "PLAYER_LOGIN");
    byte[] publicKey = KEY_PAIR.getPublic().getEncoded();
    byte[] verifyToken = new byte[]{3, 1, 4, 1, 5, 9};
    byte[] payload = binaryPayload(output -> {
      output.writeUTF(String.join(",", clientboundPackets));
      output.writeUTF(String.join(",", serverboundPackets));
      output.writeUTF("AES/CFB8/NoPadding");
      output.writeUTF("ZLIB");
      output.writeUTF("HmacSHA256");
      output.writeInt(publicKey.length);
      output.write(publicKey);
      output.writeInt(verifyToken.length);
      output.write(verifyToken);
    });
    return fixture(
      CLIENTBOUND, "HELLO", "0", BINARY, payload,
      packet -> {
        ClientboundHello hello = (ClientboundHello) packet;
        assertEquals(clientboundPackets, hello.clientboundPackets());
        assertEquals(serverboundPackets, hello.serverboundPackets());
        assertEquals("AES/CFB8/NoPadding", hello.encryptionAlgorithm());
        assertEquals("ZLIB", hello.compressionAlgorithm());
        assertEquals("HmacSHA256", hello.hmacAlgorithm());
        assertArrayEquals(publicKey, hello.publicKey().getEncoded());
        assertArrayEquals(verifyToken, hello.verifyToken());
      }
    );
  }

  private static KeyPair rsaKeyPair() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(1024);
      return generator.generateKeyPair();
    } catch (Exception exception) {
      throw new IllegalStateException(
        "Unable to generate clientbound hello test key",
        exception
      );
    }
  }
}
