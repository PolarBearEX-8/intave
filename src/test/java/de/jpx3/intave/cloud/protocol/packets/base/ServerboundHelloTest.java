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

import java.util.Arrays;
import java.util.Collections;

import static de.jpx3.intave.cloud.protocol.Direction.SERVERBOUND;
import static de.jpx3.intave.cloud.protocol.TransferMode.BINARY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ServerboundHelloTest extends PacketSerializationTest {
  @Override
  protected PacketFixture fixture() {
    byte[] payload = binaryPayload(output -> {
      output.writeUTF("ct_test-token");
      output.writeUTF("RSA/ECB/PKCS1Padding,AES/CFB8/NoPadding");
      output.writeUTF("128,256");
      output.writeUTF("ZLIB,NONE");
      output.writeUTF("HmacSHA256");
      output.writeInt(1);
      output.writeUTF("VIOLATION");
      output.writeUTF("1");
      output.writeUTF("JSON");
      output.writeInt(1);
      output.writeUTF("REPORT");
      output.writeUTF("1");
      output.writeUTF("JSON");
    });
    return fixture(
      SERVERBOUND, "HELLO", "0", BINARY, payload,
      packet -> {
        ServerboundHello hello = (ServerboundHello) packet;
        assertEquals("ct_test-token", hello.token());
        assertEquals(
          Arrays.asList(
            "RSA/ECB/PKCS1Padding",
            "AES/CFB8/NoPadding"
          ),
          hello.supportedEncryptionAlgorithms()
        );
        assertEquals(
          Arrays.asList("ZLIB", "NONE"),
          hello.supportedCompressionAlgorithms()
        );
        assertEquals(
          Collections.singletonList("HmacSHA256"),
          hello.supportedHMACAlgorithms()
        );
        assertTrue(hello.protocol().containsKey("VIOLATION"));
      }
    );
  }
}
