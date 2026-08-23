package de.jpx3.intave.connect;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DomainCacheTest {
  @Test
  void emptyDomainCacheFallsBackToStandardDomains() {
    DomainCache cache = DomainCache.of(Collections.emptyMap(), Collections.emptyMap());

    assertFalse(cache.valid());
    assertEquals("intave.de", cache.baseDomain());
    assertEquals(Collections.singletonList("intave.de"), cache.baseDomains());
    assertEquals("service.intave.de", cache.serviceDomain());
    assertEquals(Collections.singletonList("service.intave.de"), cache.serviceDomains());
  }

  @Test
  void populatedDomainCacheRemainsValid() {
    DomainCache cache = DomainCache.of(
      Collections.singletonMap("base.example", 10L),
      Collections.singletonMap("service.example", 20L)
    );

    assertTrue(cache.valid());
    assertEquals(Collections.singletonList("base.example"), cache.baseDomains());
    assertEquals(Collections.singletonList("service.example"), cache.serviceDomains());
  }
}
