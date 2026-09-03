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

final class Triangular implements RandomGenerator {
  private final double min;
  private final double mode;
  private final double max;
  private final double range;
  private final double split;

  public Triangular(double min, double mode, double max) {
    RandomGenerators.requireFinite("min", min);
    RandomGenerators.requireFinite("mode", mode);
    RandomGenerators.requireFinite("max", max);
    if (min >= max || mode < min || mode > max) {
      throw new IllegalArgumentException("triangular parameters must satisfy min < max and min <= mode <= max");
    }
    double range = max - min;
    RandomGenerators.requireFinite("triangular range", range);
    this.min = min;
    this.mode = mode;
    this.max = max;
    this.range = range;
    this.split = (mode - min) / range;
  }

  @Override
  public double next(Random random) {
    double sample = random.nextDouble();
    if (sample < split) {
      return min + Math.sqrt(sample * range * (mode - min));
    }
    return max - Math.sqrt((1.0 - sample) * range * (max - mode));
  }
}
