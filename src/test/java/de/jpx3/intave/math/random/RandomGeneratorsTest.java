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
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class RandomGeneratorsTest {
  @Test
  void givesEveryGeneratorItsOwnClass() {
    assertEquals(Constant.class, RandomGenerators.constant(0.5).getClass());
    assertEquals(Uniform.class, RandomGenerators.uniform(0.0, 1.0).getClass());
    assertEquals(Normal.class, RandomGenerators.normal(0.0, 1.0).getClass());
    assertEquals(Gaussian.class, RandomGenerators.gaussian(0.0, 1.0).getClass());
    assertEquals(Triangular.class, RandomGenerators.triangular(0.0, 0.5, 1.0).getClass());
    assertEquals(Exponential.class, RandomGenerators.exponential(1.0).getClass());
    assertEquals(Choice.class, RandomGenerators.choice(0.0, 1.0).getClass());
    assertEquals(
      WeightedChoice.class,
      RandomGenerators.weightedChoice(new double[] {0.0, 1.0}, new double[] {1.0, 1.0}).getClass()
    );
  }

  @Test
  void exposesDistributionFactoriesWithoutJson() {
    FixedRandom random = new FixedRandom(0.25, -2.0);

    assertEquals(0.3, RandomGenerators.uniform(0.2, 0.6).next(random), 1.0E-12);
    assertEquals(0.5, RandomGenerators.normal(0.7, 0.1).next(random), 1.0E-12);
    assertEquals(
      Math.sqrt(0.125),
      RandomGenerators.triangular(0.0, 0.5, 1.0).next(random),
      1.0E-12
    );
    assertEquals(
      -Math.log(0.75) / 2.0,
      RandomGenerators.exponential(2.0).next(random),
      1.0E-12
    );
  }

  @Test
  void supportsWeightedChoicesAndDefensiveCopies() {
    double[] values = {0.2, 0.8};
    double[] weights = {1.0, 3.0};
    RandomGenerator generator = RandomGenerators.weightedChoice(values, weights);
    values[1] = 10.0;
    weights[1] = 0.0;

    assertEquals(0.2, generator.next(new FixedRandom(0.1, 0.0)));
    assertEquals(0.8, generator.next(new FixedRandom(0.8, 0.0)));
  }

  @Test
  void parsesTheReusableJsonSpecification() {
    JsonObject parameters = new JsonObject();
    parameters.addProperty("mean", 0.7);
    parameters.addProperty("standardDeviation", 0.1);
    JsonObject specification = new JsonObject();
    specification.addProperty("function", "normal");
    specification.add("parameters", parameters);

    RandomGenerator generator = RandomParser.parse(specification);

    assertEquals(-0.3, generator.next(new FixedRandom(0.0, -10.0)), 1.0E-12);
    assertEquals(1.7, generator.next(new FixedRandom(0.0, 10.0)), 1.0E-12);
  }

  @Test
  void rejectsInvalidFactoryArguments() {
    assertThrows(IllegalArgumentException.class, () -> RandomGenerators.uniform(1.0, 0.0));
    assertThrows(IllegalArgumentException.class, () -> RandomGenerators.normal(0.0, -1.0));
    assertThrows(
      IllegalArgumentException.class,
      () -> RandomGenerators.weightedChoice(new double[] {1.0}, new double[] {0.0})
    );
  }

  private static final class FixedRandom extends Random {
    private static final long serialVersionUID = 1L;

    private final double nextDouble;
    private final double nextGaussian;

    private FixedRandom(double nextDouble, double nextGaussian) {
      this.nextDouble = nextDouble;
      this.nextGaussian = nextGaussian;
    }

    @Override
    public double nextDouble() {
      return nextDouble;
    }

    @Override
    public double nextGaussian() {
      return nextGaussian;
    }
  }
}
