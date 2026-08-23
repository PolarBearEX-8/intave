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

package de.jpx3.intave.module.nayoro;

import ac.intave.samples.event.BlockUpdatesEvent;
import ac.intave.samples.event.Event;
import ac.intave.samples.serial.JsonReader;
import ac.intave.samples.serial.JsonWriter;
import ac.intave.samples.share.Block;
import ac.intave.samples.share.BlockPosition;
import ac.intave.samples.share.BlockUpdate;
import ac.intave.samples.share.BoundingBox;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RecordEventSinkTest {
  @Test
  void splitsComplexUpdatesIntoConsecutiveReadableEvents() throws Exception {
    List<BlockUpdate> updates = updates(24, 40);

    List<BlockUpdatesEvent> events = RecordEventSink.chunkBlockUpdates(updates);

    assertTrue(events.size() > 1);
    List<BlockUpdate> combined = new ArrayList<>();
    StringWriter output = new StringWriter();
    JsonWriter writer = new JsonWriter(output);
    for (BlockUpdatesEvent event : events) {
      assertTrue(
        RecordEventSink.serializedCharacters(event) <=
          RecordEventSink.EVENT_CHARACTER_BUDGET
      );
      combined.addAll(event.updates());
      writer.visitAny(event);
    }
    assertEquals(updates, combined);

    int decodedEvents = 0;
    try (JsonReader reader = new JsonReader(new StringReader(output.toString()))) {
      Event event;
      while ((event = reader.nextEvent()) != null) {
        assertInstanceOf(BlockUpdatesEvent.class, event);
        decodedEvents++;
      }
    }
    assertEquals(events.size(), decodedEvents);
  }

  @Test
  void rejectsASingleUpdateThatCannotFit() {
    List<BlockUpdate> update = updates(1, 400);

    assertThrows(
      IllegalArgumentException.class,
      () -> RecordEventSink.chunkBlockUpdates(update)
    );
  }

  private static List<BlockUpdate> updates(int updateCount, int boxesPerBlock) {
    List<BoundingBox> boxes = new ArrayList<>();
    for (int index = 0; index < boxesPerBlock; index++) {
      double min = index / (double) boxesPerBlock;
      double max = (index + 1) / (double) boxesPerBlock;
      boxes.add(new BoundingBox(min, 0.0, 0.0, max, 1.0, 1.0));
    }
    Block block = new Block("TEST_BLOCK", Collections.emptyMap(), boxes);
    List<BlockUpdate> updates = new ArrayList<>();
    for (int index = 0; index < updateCount; index++) {
      updates.add(new BlockUpdate(new BlockPosition(index, 64, 0), block));
    }
    return updates;
  }
}
