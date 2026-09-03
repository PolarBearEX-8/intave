package de.jpx3.intave.module.linker.bukkit;

import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.check.EventProcessor;
import de.jpx3.intave.diagnostic.timings.Timing;
import de.jpx3.intave.diagnostic.timings.Timings;
import de.jpx3.intave.module.linker.SubscriptionInstanceProvider;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

public final class IntaveRegisteredListener extends RegisteredListener {
  private final IntavePlugin plugin;
  private final EventExecutor eventExecutor;
  private final SubscriptionInstanceProvider<? super Event, ?, ? extends BukkitEventSubscriber> listenerProvider;
  private final Class<? extends Event> eventClass;
  private Timing timing;
  private boolean checkIfCancelled;

  public IntaveRegisteredListener(
    IntavePlugin plugin,
    SubscriptionInstanceProvider<? super Event, ?, ? extends BukkitEventSubscriber> listenerProvider,
    EventExecutor eventExecutor,
    Class<? extends Event> eventClass,
    BukkitEventSubscription eventHandler
  ) {
    super(listenerProvider.fallback(), null, eventHandler.priority(), plugin, true/*eventHandler.ignoreCancelled()*/);
    this.plugin = plugin;
    this.eventExecutor = eventExecutor;
    this.eventClass = eventClass;
    this.listenerProvider = listenerProvider;
  }

  public void initialize() {
    checkIfCancelled = !isIgnoringCancelled() && Cancellable.class.isAssignableFrom(eventClass);
  }

  @Override
  public void callEvent(Event event) throws EventException {
    BukkitEventSubscriber eventSubscriber = listenerProvider.fallback();
    if (eventSubscriber instanceof EventProcessor && !((EventProcessor) eventSubscriber).shouldProcessEvents() ||
      !eventClass.isAssignableFrom(event.getClass()) ||
      checkIfCancelled && ((Cancellable) event).isCancelled()
    ) {
      return;
    }
    boolean asynchronous = !Bukkit.isPrimaryThread();
    if (!asynchronous) {
      Timings.EXE_SERVER.start();
      if (timing == null) {
        timing = Timings.eventTimingOf(event);
      }
      timing.start();
    }
    listenerProvider.apply(event, listener -> {
      try {
        eventExecutor.execute(listener, event);
      } catch (RuntimeException | EventException exception) {
        exception.printStackTrace();
      }
    });

    if (!asynchronous) {
      Timings.EXE_SERVER.stop();
      timing.stop();
    }
  }
}
