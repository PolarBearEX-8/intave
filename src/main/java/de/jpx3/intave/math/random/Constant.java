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

final class Constant implements RandomGenerator {
  private final double value;

  public Constant(double value) {
    RandomGenerators.requireFinite("value", value);
    this.value = value;
  }

  @Override
  public double next(Random random) {
    return value;
  }
}
