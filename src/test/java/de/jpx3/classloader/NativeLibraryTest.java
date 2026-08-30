package de.jpx3.classloader;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class NativeLibraryTest {
  private static final String RESOURCE_DIRECTORY = "/de/jpx3/classloader/native/v2/";

  private static final String[][] LIBRARIES = {
    {
      "libclassloader-linux-aarch64.so",
      "4900df5b06bfdd40de92dc93141f158ed7abb12143df89e1e5aa617fdac95bad"
    },
    {
      "libclassloader-linux-x86_64.so",
      "9bb42f8f9d9a526c2be70256d0d7d0e2efc4eb2550c919745f0b36257596f563"
    }
  };

  @Test
  void bundlesTrustedNativeLibraries() throws Exception {
    for (String[] library : LIBRARIES) {
      String resourcePath = RESOURCE_DIRECTORY + library[0];
      try (InputStream inputStream = NativeLibrary.class.getResourceAsStream(resourcePath)) {
        assertNotNull(inputStream, "Missing native library " + resourcePath);
        assertEquals(library[1], sha256(inputStream), "Unexpected native library " + resourcePath);
      }
    }
  }

  private static String sha256(InputStream inputStream) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] buffer = new byte[8192];
    int read;
    while ((read = inputStream.read(buffer)) != -1) {
      digest.update(buffer, 0, read);
    }

    StringBuilder hash = new StringBuilder();
    for (byte value : digest.digest()) {
      hash.append(String.format("%02x", value & 0xff));
    }
    return hash.toString();
  }
}
