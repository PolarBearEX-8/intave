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

final class Exponential implements RandomGenerator {
  private final double rate;

  public Exponential(double rate) {
    RandomGenerators.requireFinite("rate", rate);
    if (rate <= 0.0) {
      throw new IllegalArgumentException("exponential rate must be positive");
    }
    this.rate = rate;
  }

  @Override
  public double next(Random random) {
    return -Math.log1p(-random.nextDouble()) / rate;
  }
}
