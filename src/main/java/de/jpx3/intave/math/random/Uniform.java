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

final class Uniform implements RandomGenerator {
  private final double min;
  private final double range;

  public Uniform(double min, double max) {
    RandomGenerators.requireFinite("min", min);
    RandomGenerators.requireFinite("max", max);
    if (min > max) {
      throw new IllegalArgumentException("uniform min must not exceed max");
    }
    double range = max - min;
    RandomGenerators.requireFinite("uniform range", range);
    this.min = min;
    this.range = range;
  }

  @Override
  public double next(Random random) {
    return min + random.nextDouble() * range;
  }
}
