package de.jpx3.intave.block.variant;

import de.jpx3.intave.cleanup.ReferenceMap;
import org.bukkit.Material;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

public final class BlockVariantReverseLookup {
  private static final Map<Material, Map<Integer, Set<Integer>>> reverseLookup = ReferenceMap.soft(new HashMap<>());
  private static final Map<Material, Map<Map<String, Comparable<?>>, Integer>> exactReverseLookup = ReferenceMap.soft(new HashMap<>());

  public static Set<Integer> variantsOfConfiguration(
    Material material, int uniqueId,
    Function<? super String, ? extends Comparable<?>> settingResolver
  ) {
    return reverseLookup.computeIfAbsent(material, x -> new HashMap<>())
      .computeIfAbsent(uniqueId, x -> performReverseLookup(material, settingResolver));
  }

  public static int variantIdOfProperties(Material material, Map<String, ? extends Comparable<?>> properties) {
    Map<String, Comparable<?>> normalizedProperties = normalizeProperties(properties);
    return exactReverseLookup.computeIfAbsent(material, x -> new HashMap<>())
      .computeIfAbsent(normalizedProperties, x -> performExactReverseLookup(material, normalizedProperties));
  }

  private static Set<Integer> performReverseLookup(Material material, Function<? super String, ? extends Comparable<?>> configMatcher) {
    Set<Integer> integers = BlockVariantRegister.variantIdsOf(material);
    integers.removeIf(integer -> {
      BlockVariant config = BlockVariantRegister.variantOf(material, integer);
      for (String propertyName : config.propertyNames()) {
        Object property = config.propertyOf(propertyName);
        Comparable<?> value = configMatcher.apply(propertyName);
        if (value == null) {
          continue;
        }
        if (value.getClass().isEnum()) {
          // enums are translated to strings
          value = ((Enum<?>) value).name();
        }
        if (!Objects.equals(property, value)) {
          return true;
        }
      }
      return false;
    });
    return integers;
  }

  private static int performExactReverseLookup(Material material, Map<String, Comparable<?>> properties) {
    int matchedVariant = -1;
    for (Integer integer : BlockVariantRegister.variantIdsOf(material)) {
      BlockVariant config = BlockVariantRegister.variantOf(material, integer);
      if (matchesExactly(config, properties) && (matchedVariant == -1 || integer < matchedVariant)) {
        matchedVariant = integer;
      }
    }
    return matchedVariant;
  }

  private static boolean matchesExactly(BlockVariant config, Map<String, Comparable<?>> properties) {
    return Objects.equals(normalizeProperties(config.properties()), properties);
  }

  private static Map<String, Comparable<?>> normalizeProperties(Map<String, ? extends Comparable<?>> properties) {
    Map<String, Comparable<?>> normalizedProperties = new TreeMap<>();
    for (Map.Entry<String, ? extends Comparable<?>> entry : properties.entrySet()) {
      normalizedProperties.put(entry.getKey().toLowerCase(Locale.ROOT), normalizeProperty(entry.getValue()));
    }
    return Collections.unmodifiableMap(normalizedProperties);
  }

  private static Comparable<?> normalizeProperty(Comparable<?> value) {
    return value instanceof Enum<?> ? ((Enum<?>) value).name() : value;
  }

  public static Set<Material> cachedMaterials() {
    return reverseLookup.keySet();
  }

  static void invalidateCache() {
    reverseLookup.clear();
    exactReverseLookup.clear();
  }
}
