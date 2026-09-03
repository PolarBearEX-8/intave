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

class Normal implements RandomGenerator {
  private final double mean;
  private final double standardDeviation;

  public Normal(double mean, double standardDeviation) {
    RandomGenerators.requireFinite("mean", mean);
    RandomGenerators.requireFinite("standardDeviation", standardDeviation);
    if (standardDeviation < 0.0) {
      throw new IllegalArgumentException("standardDeviation must be non-negative");
    }
    this.mean = mean;
    this.standardDeviation = standardDeviation;
  }

  @Override
  public double next(Random random) {
    return mean + random.nextGaussian() * standardDeviation;
  }
}
