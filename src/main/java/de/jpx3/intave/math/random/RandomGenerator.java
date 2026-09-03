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

package de.jpx3.intave.math.random;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/** A reusable source of random {@code double} values. */
@FunctionalInterface
public interface RandomGenerator {
  double next(Random random);

  default double next() {
    return next(ThreadLocalRandom.current());
  }
}
