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

package de.jpx3.intave.cloud.request;

import de.jpx3.intave.access.player.trust.TrustFactor;
import de.jpx3.intave.access.player.trust.TrustFactorResolver;
import de.jpx3.intave.cloud.Cloud;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public final class CloudTrustfactorResolver implements TrustFactorResolver {
  private final Cloud cloud;

  public CloudTrustfactorResolver(Cloud cloud) {
    this.cloud = cloud;
  }

  @Override
  public void resolve(Player player, Consumer<TrustFactor> callback) {
    cloud.trustfactorRequest(player, callback);
  }

  @Override
  public String toString() {
    return "CloudTrustfactorResolver";
  }
}
