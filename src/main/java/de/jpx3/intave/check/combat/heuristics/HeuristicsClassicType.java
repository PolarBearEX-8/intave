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

package de.jpx3.intave.check.combat.heuristics;

public enum HeuristicsClassicType {
  ATTACK_ACCURACY("attack-accuracy"),
  ATTACK_REQUIRED("attack-required"),
  PRE_ATTACK("pre-attack"),
  ROTATION_ACCURACY("rotation-accuracy"),
  ROTATION_EXACT("rotation-exact"),
  ROTATION_SNAP("rotation-snap"),
  ROTATION_SENSITIVITY("rotation-sensitivity"),
  ROTATION_MODULO_RESET("rotation-reset"),
  INVENTORY_ROTATIONS("inventory-rotations"),
  BLOCKING("blocking"),
  NO_SWING("no-swing"),
  SWING_ORDER("swing-order"),
  SPRINT_TOGGLES("sprint-toggles"),
  TOOL_SWITCH("tool-switch");

  private final String configurationName;

  HeuristicsClassicType(String configurationName) {
    this.configurationName = configurationName;
  }

  public String configurationName() {
    return configurationName;
  }

  public String verboseName() {
    return configurationName;
  }

  public String displayName() {
    return configurationName.replace('-', ' ');
  }
}
