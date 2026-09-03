package de.jpx3.intave.check;

import de.jpx3.intave.module.linker.bukkit.BukkitEventSubscriber;
import de.jpx3.intave.module.linker.nayoro.NayoroEventSubscriber;
import de.jpx3.intave.module.linker.packet.PacketEventSubscriber;

/**
 * A combination of a {@link BukkitEventSubscriber} and a {@link PacketEventSubscriber},
 * unifying subscription receiver tags
 */
public interface EventProcessor extends BukkitEventSubscriber,
                                        PacketEventSubscriber,
                                        NayoroEventSubscriber {
  /**
   * Returns whether events should currently be forwarded to this processor.
   * Non-check processors are active by default.
   *
   * @return whether this processor should receive events
   */
  default boolean shouldProcessEvents() {
    return true;
  }
}
