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

import ac.intave.samples.event.*;
import ac.intave.samples.serial.JsonWriter;
import ac.intave.samples.share.Classifier;
import de.jpx3.intave.adapter.MinecraftVersion;
import de.jpx3.intave.module.nayoro.stream.PeriodicFlushOutputStream;
import de.jpx3.intave.module.tracker.entity.Entity;
import de.jpx3.intave.version.ProtocolVersionConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class RecordEventSink extends EventSink {
  private static final int COMPRESSION_FLUSH_THRESHOLD = 128 * 1024;

  private final long startedAt = System.currentTimeMillis();
  private long lastEventAt = startedAt;
  private final Environment environment;
  private final OutputStream output;
  private JsonWriter writer;
  private final Set<Integer> entities = new HashSet<>();
  private boolean setup = false;
  private final Classifier classifier;
  private final Lock writeLock = new ReentrantLock();

  public RecordEventSink(Environment environment, OutputStream output) {
    this.environment = environment;
    this.output = output;
    this.classifier = Classifier.UNKNOWN;
  }

  public RecordEventSink(Environment environment, OutputStream output, Classifier classifier) {
    this.environment = environment;
    this.output = output;
    this.classifier = classifier == null ? Classifier.UNKNOWN : classifier;
  }

  public synchronized void setupIfNeeded() {
    if (!setup) {
      try {
        writeLock.lock();
        JsonWriter initializedWriter = new JsonWriter(
          new PeriodicFlushOutputStream(output, COMPRESSION_FLUSH_THRESHOLD)
        );
        initializedWriter.visitAny(
          new HeaderEvent(UUID.randomUUID(), "unknown", classifier, startedAt)
        );
        writer = initializedWriter;
        setup = true;
      } catch (IOException exception) {
        throw new IllegalStateException("Could not initialize recording writer", exception);
      } finally {
        writeLock.unlock();
      }
      PlayerContainer player = environment.mainPlayer();
      visit(new PlayerInitEvent(
        player.name(), player.uuid(), player.id(), player.version(),
        ProtocolVersionConverter.protocolVersionBy(MinecraftVersion.current()),
        SampleTypes.position(player.position()), SampleTypes.rotation(player.rotation())
      ));
      visit(new PropertiesEvent(environment.properties()));
      environment.mainPlayer().applyIfUserPresent(user -> {
        for (Entity tracedEntity : user.meta().connection().tracedEntities()) {
          visit(new EntitySpawnEvent(
            tracedEntity.entityId(), tracedEntity.entityName(),
            SampleTypes.hitboxSize(tracedEntity.typeData().size()),
            SampleTypes.position(tracedEntity.position.toPosition())
          ));
        }
      });
    }
  }

  @Override
  public void visit(EntitySpawnEvent event) {
    entities.add(event.id());
    visitAny(event);
  }

  @Override
  public void visit(AttackEvent event) {
    if (isIdInContextCurrent(event.source()) && isIdInContextCurrent(event.target())) {
      visitAny(event);
    }
  }

  private boolean isIdInContextCurrent(int id) {
    return entities.contains(id) || environment.mainPlayer().id() == id;
  }

  @Override
  public void visit(EntityMoveEvent event) {
    if (entities.contains(event.entityId())) {
      visitAny(event);
    }
  }

  @Override
  public void visit(EntityRemoveEvent event) {
    if (entities.remove(event.id())) {
      visitAny(event);
    }
  }

  @Override
  public synchronized void visitAny(Event event) {
    setupIfNeeded();
    try {
      writeLock.lock();
      long now = System.currentTimeMillis();
      event.withOffset(Math.max(0, now - lastEventAt));
      lastEventAt = now;
      writer.visitAny(event);
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public void close() {
    setupIfNeeded();
    try {
      writeLock.lock();
      writer.close();
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public String name() {
    return "RECORD";
  }
}
