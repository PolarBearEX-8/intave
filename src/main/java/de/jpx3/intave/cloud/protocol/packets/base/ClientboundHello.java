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

import de.jpx3.intave.cloud.protocol.BinaryPacket;
import de.jpx3.intave.cloud.protocol.listener.Clientbound;

import java.io.DataInput;
import java.io.DataOutput;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.jpx3.intave.cloud.protocol.Direction.CLIENTBOUND;

public final class ClientboundHello extends BinaryPacket<Clientbound> {
  private List<String> clientboundPackets = new ArrayList<>();
  private List<String> serverboundPackets = new ArrayList<>();
  private String encryptionAlgorithm;
  private String compressionAlgorithm;
  private String hmacAlgorithm;

  private PublicKey publicKey;
  private byte[] verifyToken;

  public ClientboundHello() {
    super(CLIENTBOUND, "HELLO", "0");
  }

  public ClientboundHello(
    List<String> clientboundPackets,
    List<String> serverboundPackets,
    String encryptionAlgorithm, String compressionAlgorithm, String hmacAlgorithm,
    PublicKey publicKey, byte[] verifyToken
  ) {
    super(CLIENTBOUND, "HELLO", "0");
    this.clientboundPackets = clientboundPackets;
    this.serverboundPackets = serverboundPackets;
    this.encryptionAlgorithm = encryptionAlgorithm;
    this.compressionAlgorithm = compressionAlgorithm;
    this.hmacAlgorithm = hmacAlgorithm;
    this.publicKey = publicKey;
    this.verifyToken = verifyToken;
  }

  @Override
  public void serialize(DataOutput buffer) {
    try {
      buffer.writeUTF(String.join(",", clientboundPackets));
      buffer.writeUTF(String.join(",", serverboundPackets));
      buffer.writeUTF(encryptionAlgorithm);
      buffer.writeUTF(compressionAlgorithm);
      buffer.writeUTF(hmacAlgorithm);
      byte[] encoded = publicKey.getEncoded();
      buffer.writeInt(encoded.length);
      buffer.write(encoded);
      buffer.writeInt(verifyToken.length);
      buffer.write(verifyToken);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to serialize clientbound hello packet", e);
    }
  }

  @Override
  public void deserialize(DataInput buffer) {
    try {
      clientboundPackets = new ArrayList<>();
      clientboundPackets.addAll(Arrays.asList(buffer.readUTF().split(",")));
      serverboundPackets = new ArrayList<>();
      serverboundPackets.addAll(Arrays.asList(buffer.readUTF().split(",")));
      encryptionAlgorithm = buffer.readUTF();
      compressionAlgorithm = buffer.readUTF();
      hmacAlgorithm = buffer.readUTF();
      int size = buffer.readInt();
      if (size > 1024 * 8) {
        throw new RuntimeException("Invalid public key size");
      }
      byte[] publicKey = new byte[size];
      buffer.readFully(publicKey, 0, size);
      // get the public key of key exchange algorithm
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
      this.publicKey = keyFactory.generatePublic(keySpec);
      size = buffer.readInt();
      if (size > 1024 * 8) {
        throw new RuntimeException("Invalid verify token size");
      }
      verifyToken = new byte[size];
      buffer.readFully(verifyToken, 0, size);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to deserialize clientbound hello packet", e);
    }
  }

  public List<String> clientboundPackets() {
    return clientboundPackets;
  }

  public List<String> serverboundPackets() {
    return serverboundPackets;
  }

  public String encryptionAlgorithm() {
    return encryptionAlgorithm;
  }

  public String compressionAlgorithm() {
    return compressionAlgorithm;
  }

  public String hmacAlgorithm() {
    return hmacAlgorithm;
  }

  public PublicKey publicKey() {
    return publicKey;
  }

  public byte[] verifyToken() {
    return verifyToken;
  }
}
