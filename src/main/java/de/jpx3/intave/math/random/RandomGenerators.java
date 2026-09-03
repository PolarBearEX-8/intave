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

import com.google.gson.JsonObject;

/** Stable factory facade for the concrete random-generator implementations. */
public final class RandomGenerators {
  private RandomGenerators() {
  }

  public static RandomGenerator constant(double value) {
    return new Constant(value);
  }

  public static RandomGenerator uniform(double min, double max) {
    return new Uniform(min, max);
  }

  public static RandomGenerator normal(double mean, double standardDeviation) {
    return new Normal(mean, standardDeviation);
  }

  public static RandomGenerator gaussian(double mean, double standardDeviation) {
    return new Gaussian(mean, standardDeviation);
  }

  public static RandomGenerator triangular(double min, double mode, double max) {
    return new Triangular(min, mode, max);
  }

  public static RandomGenerator exponential(double rate) {
    return new Exponential(rate);
  }

  public static RandomGenerator choice(double... values) {
    return new Choice(values);
  }

  public static RandomGenerator weightedChoice(double[] values, double[] weights) {
    return new WeightedChoice(values, weights);
  }

  public static RandomGenerator parseFrom(JsonObject json) {
    return RandomParser.parse(json);
  }

  static void requireFinite(String name, double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      throw new IllegalArgumentException(name + " must be finite");
    }
  }

  static double[] copyValues(double[] values) {
    if (values == null || values.length == 0) {
      throw new IllegalArgumentException("values must not be empty");
    }
    double[] copy = values.clone();
    for (int i = 0; i < copy.length; i++) {
      requireFinite("values[" + i + "]", copy[i]);
    }
    return copy;
  }
}
