package de.jpx3.intave.world.raytrace;

import de.jpx3.intave.share.Position;
import de.jpx3.intave.share.RawVector3d;

public final class Raytrace {
  /** A non-colliding marker outside the interaction-range attribute's finite [0, 64] domain. */
  public static final double MISS_DISTANCE = Double.POSITIVE_INFINITY;

  private final Position from;
  private final Position to;
  private final double reach;

  public Raytrace(Position from, Position to, double distance) {
    this.from = from;
    this.to = to;
    this.reach = distance;
  }

  public Position from() {
    return from;
  }

  public Position to() {
    return to;
  }

  public double reach() {
    return reach;
  }

  public boolean missed() {
    return reach == MISS_DISTANCE;
  }

  public static Raytrace ofNative(
    RawVector3d nativeEyeVector,
    RawVector3d nativeTargetVector,
    double reach
  ) {
    return new Raytrace(nativeEyeVector.toPosition(), nativeTargetVector == null ? null : nativeTargetVector.toPosition(), reach);
  }
}
