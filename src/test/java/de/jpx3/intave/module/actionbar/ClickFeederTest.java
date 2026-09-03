package de.jpx3.intave.module.actionbar;

import org.junit.jupiter.api.Test;

import static de.jpx3.intave.module.actionbar.ClickFeeder.TickAction.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ClickFeederTest {
  @Test
  void reconstructsTwentyPositionReminderTicks() {
    ClickFeeder.ClickBufferData buffer = new ClickFeeder.ClickBufferData(null);
    buffer.recordClick(50);
    buffer.recordAttack(50);
    buffer.recordClick(950);
    buffer.recordPlace(950);
    buffer.recordClick(1_000);
    buffer.recordClick(1_000);

    ClickFeeder.appendBufferedTicks(buffer, 1_000, 20);

    assertEquals(ATTACK, buffer.actionAtAge(19));
    assertEquals(1, buffer.intensityAtAge(19));
    assertEquals(PLACE, buffer.actionAtAge(1));
    assertEquals(1, buffer.intensityAtAge(1));
    assertEquals(CLICK, buffer.actionAtAge(0));
    assertEquals(2, buffer.intensityAtAge(0));
    assertEquals(NOTHING, buffer.actionAtAge(18));
  }

  @Test
  void excludesActionsOlderThanPositionReminderWindow() {
    ClickFeeder.ClickBufferData buffer = new ClickFeeder.ClickBufferData(null);
    buffer.recordAttack(0);

    ClickFeeder.appendBufferedTicks(buffer, 1_000, 20);

    for (int age = 0; age < 20; age++) {
      assertEquals(NOTHING, buffer.actionAtAge(age));
      assertEquals(0, buffer.intensityAtAge(age));
    }
  }
}
