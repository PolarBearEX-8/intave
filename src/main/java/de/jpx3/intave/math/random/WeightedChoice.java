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

final class WeightedChoice implements RandomGenerator {
  private final double[] values;
  private final double[] cumulativeWeights;
  private final double totalWeight;

  public WeightedChoice(double[] values, double[] weights) {
    this.values = RandomGenerators.copyValues(values);
    if (weights == null || weights.length != this.values.length) {
      throw new IllegalArgumentException("weights must have the same length as values");
    }

    this.cumulativeWeights = new double[weights.length];
    double totalWeight = 0.0;
    for (int i = 0; i < weights.length; i++) {
      double weight = weights[i];
      RandomGenerators.requireFinite("weights[" + i + "]", weight);
      if (weight < 0.0) {
        throw new IllegalArgumentException("weights must be non-negative");
      }
      totalWeight += weight;
      RandomGenerators.requireFinite("weight total", totalWeight);
      cumulativeWeights[i] = totalWeight;
    }
    if (totalWeight <= 0.0) {
      throw new IllegalArgumentException("at least one weight must be positive");
    }
    this.totalWeight = totalWeight;
  }

  @Override
  public double next(Random random) {
    double selection = random.nextDouble() * totalWeight;
    for (int i = 0; i < cumulativeWeights.length; i++) {
      if (selection < cumulativeWeights[i]) {
        return values[i];
      }
    }
    return values[values.length - 1];
  }
}
