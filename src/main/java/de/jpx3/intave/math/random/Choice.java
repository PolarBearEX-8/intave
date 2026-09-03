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

final class Choice implements RandomGenerator {
  private final double[] values;

  public Choice(double... values) {
    this.values = RandomGenerators.copyValues(values);
  }

  @Override
  public double next(Random random) {
    return values[random.nextInt(values.length)];
  }
}
