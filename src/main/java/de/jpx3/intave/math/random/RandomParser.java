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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Locale;

final class RandomParser {
  private static final int MAX_CHOICE_COUNT = 64;

  private RandomParser() {}

  /**
   * Parses specifications shaped as:
   * <pre>
   * {
   *   "function": "uniform",
   *   "parameters": {"min": 0.5, "max": 0.9}
   * }
   * </pre>
   */
  static RandomGenerator parse(JsonObject specification) {
    if (specification == null) {
      throw new IllegalArgumentException("random generator specification is required");
    }

    String function = requiredString(specification, "function")
      .trim()
      .toLowerCase(Locale.ROOT)
      .replace('-', '_');
    JsonObject parameters = optionalObject(specification, "parameters");
    RandomGenerator generator;
    switch (function) {
      case "constant":
      case "fixed":
        generator = RandomGenerators.constant(requiredNumber(parameters, "value"));
        break;
      case "uniform":
        generator = RandomGenerators.uniform(
          requiredNumber(parameters, "min"),
          requiredNumber(parameters, "max")
        );
        break;
      case "normal":
        generator = RandomGenerators.normal(
          requiredNumber(parameters, "mean"),
          numberWithAlias(parameters, "standardDeviation", "stddev")
        );
        break;
      case "gaussian":
        generator = RandomGenerators.gaussian(
          requiredNumber(parameters, "mean"),
          numberWithAlias(parameters, "standardDeviation", "stddev")
        );
        break;
      case "triangular":
        generator = RandomGenerators.triangular(
          requiredNumber(parameters, "min"),
          requiredNumber(parameters, "mode"),
          requiredNumber(parameters, "max")
        );
        break;
      case "exponential":
        generator = RandomGenerators.exponential(numberWithAlias(parameters, "rate", "lambda"));
        break;
      case "choice":
        generator = choice(parameters, false);
        break;
      case "weighted":
      case "weighted_choice":
        generator = choice(parameters, true);
        break;
      default:
        throw new IllegalArgumentException("unsupported random function '" + function + "'");
    }

    return generator;
  }

  private static RandomGenerator choice(JsonObject parameters, boolean weightsRequired) {
    JsonArray valuesJson = requiredArray(parameters, "values");
    if (valuesJson.size() == 0 || valuesJson.size() > MAX_CHOICE_COUNT) {
      throw new IllegalArgumentException("choice values must contain between 1 and 64 entries");
    }
    double[] values = numbers(valuesJson, "parameters.values");

    JsonElement weightsElement = parameters.get("weights");
    if (weightsElement == null) {
      if (weightsRequired) {
        throw new IllegalArgumentException("parameters.weights is required for a weighted choice");
      }
      return RandomGenerators.choice(values);
    }
    if (!weightsElement.isJsonArray()) {
      throw new IllegalArgumentException("parameters.weights must be an array");
    }
    JsonArray weightsJson = weightsElement.getAsJsonArray();
    if (weightsJson.size() != values.length) {
      throw new IllegalArgumentException("choice weights must have the same length as values");
    }
    return RandomGenerators.weightedChoice(values, numbers(weightsJson, "parameters.weights"));
  }

  private static double[] numbers(JsonArray array, String name) {
    double[] values = new double[array.size()];
    for (int i = 0; i < values.length; i++) {
      JsonElement element = array.get(i);
      if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
        throw new IllegalArgumentException(name + "[" + i + "] must be a number");
      }
      values[i] = element.getAsDouble();
    }
    return values;
  }

  private static String requiredString(JsonObject object, String name) {
    JsonElement element = object.get(name);
    if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
      throw new IllegalArgumentException(name + " must be a string");
    }
    return element.getAsString();
  }

  private static double requiredNumber(JsonObject object, String name) {
    JsonElement element = object.get(name);
    if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
      throw new IllegalArgumentException(name + " must be a number");
    }
    return element.getAsDouble();
  }

  private static double numberWithAlias(JsonObject object, String name, String alias) {
    return object.has(name) ? requiredNumber(object, name) : requiredNumber(object, alias);
  }

  private static JsonObject optionalObject(JsonObject object, String name) {
    JsonElement element = object.get(name);
    if (element == null) {
      return new JsonObject();
    }
    if (!element.isJsonObject()) {
      throw new IllegalArgumentException(name + " must be an object");
    }
    return element.getAsJsonObject();
  }

  private static JsonArray requiredArray(JsonObject object, String name) {
    JsonElement element = object.get(name);
    if (element == null || !element.isJsonArray()) {
      throw new IllegalArgumentException(name + " must be an array");
    }
    return element.getAsJsonArray();
  }
}
