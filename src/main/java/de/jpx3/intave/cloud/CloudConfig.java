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

package de.jpx3.intave.cloud;

import de.jpx3.intave.IntaveLogger;
import de.jpx3.intave.annotate.Nullable;
import de.jpx3.intave.cloud.protocol.CloudToken;
import de.jpx3.intave.share.Result;
import org.bukkit.configuration.ConfigurationSection;

public final class CloudConfig {
  private boolean enabled;
  private CloudToken connectionSettings;
  private PrivacyMode privacyMode;
  private CloudFeatures features;

  public boolean isEnabled() {
    return this.enabled;
  }

  public CloudFeatures features() {
    return this.features;
  }

  public static CloudConfig from(ConfigurationSection section) {
    // enabled by default
    boolean enabled = section == null || section.getBoolean("enabled", true);
    ConfigurationSection featuresSection = section == null ? null : section.getConfigurationSection("features");
    boolean cloudStorage = featuresSection == null || featuresSection.getBoolean("storage", featuresSection.getBoolean("cloud-storage", true));
    boolean cloudTrustFactor = featuresSection == null || featuresSection.getBoolean("trustfactor", featuresSection.getBoolean("cloud-trustfactor", true));
    boolean cloudSamples = featuresSection == null || featuresSection.getBoolean("samples",  featuresSection.getBoolean("cloud-heuristics", true));
    boolean cloudLogs = true;//featuresSection == null || featuresSection.getBoolean("logs", featuresSection.getBoolean("cloud-logs", true));
    PrivacyMode privacyMode = PrivacyMode.fromString(section == null ? "BEST_DETECTION" : section.getString("privacy-mode", "BEST_DETECTION"));
    String cloudToken = section == null ? null : section.getString("token");
    CloudToken connectionSettings = null;
    if (cloudToken != null) {
      Result<CloudToken, String> connectionStringResult = CloudToken.fromString(cloudToken);
      if (connectionStringResult.erroneous()) {
        IntaveLogger.logger().error("Unable to read cloud token: " + connectionStringResult.error());
      } else {
        connectionSettings = connectionStringResult.result();
      }
    } else {
      IntaveLogger.logger().info("No cloud token provided, cloud will not be enabled");
    }
    CloudFeatures features = new CloudFeatures();
    features.cloudStorage = cloudStorage;
    features.cloudTrustFactor = cloudTrustFactor;
    features.cloudSamples = cloudSamples;
    features.cloudLogs = cloudLogs;
    CloudConfig config = new CloudConfig();
    config.enabled = enabled;
    config.connectionSettings = connectionSettings;
    config.features = features;
    config.privacyMode = privacyMode;
    return config;
  }

  public @Nullable CloudToken connectionSettings() {
    return this.connectionSettings;
  }

  public boolean hasConnectionSettings() {
    return this.connectionSettings != null;
  }

  public PrivacyMode privacy() {
    return this.privacyMode;
  }

  public static class CloudFeatures {
    private boolean cloudStorage;
    private boolean cloudTrustFactor;
    private boolean cloudSamples;
    private boolean cloudLogs;

    public boolean cloudStorageEnabled() {
      return this.cloudStorage;
    }

    public boolean cloudTrustfactorEnabled() {
      return this.cloudTrustFactor;
    }

    public boolean sampleTransmission() {
      return this.cloudSamples;
    }

    public boolean isCloudLogs() {
      return this.cloudLogs;
    }
  }
}
