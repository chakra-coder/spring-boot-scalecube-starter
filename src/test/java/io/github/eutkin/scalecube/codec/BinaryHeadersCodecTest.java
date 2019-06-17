package io.github.eutkin.scalecube.codec;

import static org.junit.Assert.assertEquals;

import io.scalecube.services.transport.api.HeadersCodec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BinaryHeadersCodecTest {

  private HeadersCodec codec = new BinaryHeadersCodec();

  @Test
  void name() throws IOException {
    Map<String, String> headers = new HashMap<>();
    headers.put("test", "value");
    headers.put("tag", "country");
    Map<String, String> decoded = writeAndRead(headers);
    assertEquals(headers, decoded);

  }

  private Map<String, String> writeAndRead(Map<String, String> headers) throws IOException {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      codec.encode(os, headers);
      byte[] bytes = os.toByteArray();
      try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
        return codec.decode(is);
      }
    }
  }
}