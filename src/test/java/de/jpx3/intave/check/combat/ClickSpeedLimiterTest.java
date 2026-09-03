package de.jpx3.intave.check.combat;

import de.jpx3.intave.user.meta.ProtocolMetadata;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClickSpeedLimiterTest {
  @Test
  void identifiesLegacyPositionReminderThreshold() {
    assertTrue(ClickSpeedLimiter.isWithinPositionReminderThreshold(
      ProtocolMetadata.VER_1_9, 0.03, 0, 0
    ));
    assertFalse(ClickSpeedLimiter.isWithinPositionReminderThreshold(
      ProtocolMetadata.VER_1_9, Math.nextUp(0.03), 0, 0
    ));
  }

  @Test
  void identifiesModernPositionReminderThreshold() {
    assertTrue(ClickSpeedLimiter.isWithinPositionReminderThreshold(
      ProtocolMetadata.VER_1_18_2, 0.0002, 0, 0
    ));
    assertFalse(ClickSpeedLimiter.isWithinPositionReminderThreshold(
      ProtocolMetadata.VER_1_18_2, Math.nextUp(0.0002), 0, 0
    ));
  }

  @Test
  void redistributesTwentyClicksAcrossPositionReminderInterval() {
    ClickSpeedLimiter.ClickSpeedLimiterMeta meta = new ClickSpeedLimiter.ClickSpeedLimiterMeta();
    meta.lastTickTimeStamp = 0;
    meta.attackArrayIndex = 1;
    for (long timestamp = 50; timestamp <= 1_000; timestamp += 50) {
      meta.attacksDuringFlyingPackets.add(timestamp);
    }

    ClickSpeedLimiter.redistributeAttacks(meta, 1_000, 20);

    assertEquals(20, Arrays.stream(meta.attackCountArray).sum());
    assertEquals(1, meta.attackArrayIndex);
  }

  @Test
  void positionReminderAdvancesTwentyTicksRegardlessOfArrivalTime() {
    ClickSpeedLimiter.ClickSpeedLimiterMeta meta = new ClickSpeedLimiter.ClickSpeedLimiterMeta();
    meta.attackArrayIndex = 7;

    ClickSpeedLimiter.redistributeAttacks(meta, 850, 20);

    assertEquals(7, meta.attackArrayIndex);
  }

  @Test
  void dropsAttacksOutsidePositionReminderWindow() {
    ClickSpeedLimiter.ClickSpeedLimiterMeta meta = new ClickSpeedLimiter.ClickSpeedLimiterMeta();
    Arrays.fill(meta.attackCountArray, 4);
    meta.lastTickTimeStamp = 0;
    meta.attacksDuringFlyingPackets.addAll(Arrays.asList(0L, 1L, 950L, 1_000L));

    ClickSpeedLimiter.redistributeAttacks(meta, 1_000, 20);

    assertEquals(3, Arrays.stream(meta.attackCountArray).sum());
  }
}
