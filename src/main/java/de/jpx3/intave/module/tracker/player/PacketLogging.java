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

package de.jpx3.intave.module.tracker.player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.*;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.cleanup.GarbageCollector;
import de.jpx3.intave.cleanup.ShutdownTasks;
import de.jpx3.intave.module.Module;
import de.jpx3.intave.share.MovingObjectPosition;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserRepository;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

public class PacketLogging extends Module {

  private final Map<UUID, PacketAdapter> adapterMap = GarbageCollector.watch(new HashMap<>());
  private final Map<String, UUID> packetLoggers = GarbageCollector.watch(new HashMap<>());
  private final Map<UUID, PrintStream> packetLogStreams = GarbageCollector.watch(new HashMap<>());
  private final Map<UUID, File> packetLogFiles = GarbageCollector.watch(new HashMap<>());

  {
    ShutdownTasks.add(() -> {
      packetLogStreams.forEach((uuid, printStream) -> {
        printStream.flush();
        printStream.close();
      });
    });
  }

  private static final boolean TEMP_PLAYER_CHECK;

  static {
    TEMP_PLAYER_CHECK = Arrays.stream(PacketEvent.class.getMethods())
      .anyMatch(method -> method.getName().equalsIgnoreCase("isPlayerTemporary"));
  }

  private boolean isTemporary(PacketEvent event) {
    return TEMP_PLAYER_CHECK && event.isPlayerTemporary();
  }

  public void togglePacketLogging(CommandSender sender, Player target) {
    UUID userId = target.getUniqueId();
    if (packetLoggers.containsKey(sender.getName())) {
      if (!packetLoggers.get(sender.getName()).equals(userId)) {
        sender.sendMessage(IntavePlugin.prefix() + ChatColor.GREEN + "You currently can only packetlog one player at the time, contact us if you need to log multiple players at the same time.");
        sender.sendMessage(IntavePlugin.prefix() + ChatColor.GREEN + "We will stop packetlogging for " + packetLoggers.get(sender.getName()));
        userId = packetLoggers.get(sender.getName());
      } else {
        sender.sendMessage(IntavePlugin.prefix() + ChatColor.GREEN + "Packetlogging stopped");
      }
      packetLoggers.remove(sender.getName());
      stopPacketLogging(userId);
      return;
    }

    File packetLogFile = startPacketLogging(target);
    if (packetLogFile == null) {
      return;
    }
    packetLoggers.put(sender.getName(), userId);
    sender.sendMessage(IntavePlugin.prefix() + ChatColor.GREEN + "Packetlogging started for " + target.getName());
    sender.sendMessage(IntavePlugin.prefix() + "You can find it under " + packetLogFile.getAbsolutePath());
  }

  public List<String> setPacketLoggingState(Player target, boolean enabled) {
    if (target == null) {
      return Collections.emptyList();
    }
    return setPacketLoggingState(target.getUniqueId(), target, enabled);
  }

  public List<String> setPacketLoggingState(UUID userId, Player target, boolean enabled) {
    if (userId == null) {
      return Collections.emptyList();
    }
    if (enabled) {
      if (target != null && !adapterMap.containsKey(userId)) {
        startPacketLogging(target);
      }
      return Collections.emptyList();
    } else {
      return stopPacketLogging(userId);
    }
  }

  private File startPacketLogging(Player target) {
    UUID userId = target.getUniqueId();
    if (adapterMap.containsKey(userId)) {
      return null;
    }
    File logsFolder = new File(plugin.dataFolder(), "packetlogs");
    File packetLogFile = new File(logsFolder, packetLogFileName(target.getName()));
    try {
      logsFolder.mkdir();
      packetLogFile.createNewFile();
    } catch (IOException exception) {
      exception.printStackTrace();
      return null;
    }
    try {
      OutputStream stream = new FileOutputStream(packetLogFile);
      stream = new BufferedOutputStream(stream);
      PrintStream printStream = new PrintStream(stream);

      UUID finalUserId = userId;
      List<PacketType> listenerTypes = new ArrayList<>();
      for (PacketType value : PacketType.values()) {
        if (value.isSupported()) {
          listenerTypes.add(value);
        }
      }
      PacketAdapter adapter = new PacketAdapter(IntavePlugin.singletonInstance(), ListenerPriority.MONITOR, listenerTypes, ListenerOptions.SKIP_PLUGIN_VERIFIER) {
        @Override
        public void onPacketSending(PacketEvent event) {
          if (isTemporary(event)) {
            return;
          }
          if (event.getPlayer().getUniqueId().equals(finalUserId)) {
            synchronized (printStream) {
              printStream.println((System.currentTimeMillis() % 1000) + " <--out-- " + event.getPacketType().name() + (event.isCancelled() ? " (cancelled)" : "") + " " + packetContent(event.getPacket(), UserRepository.userOf(event.getPlayer())));
            }
          }
        }

        @Override
        public void onPacketReceiving(PacketEvent event) {
          if (isTemporary(event)) {
            return;
          }
          if (event.getPlayer().getUniqueId().equals(finalUserId)) {
            synchronized (printStream) {
              printStream.println((System.currentTimeMillis() % 1000) + " --in--> " + event.getPacketType().name() + (event.isCancelled() ? " (cancelled)" : "") + " " + packetContent(event.getPacket(), UserRepository.userOf(event.getPlayer())));
            }
          }
        }
      };
      adapterMap.put(userId, adapter);
      ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
      packetLogStreams.put(userId, printStream);
      packetLogFiles.put(userId, packetLogFile);
      return packetLogFile;
    } catch (FileNotFoundException exception) {
      exception.printStackTrace();
      return null;
    }
  }

  private List<String> stopPacketLogging(UUID userId) {
    PacketAdapter adapter = adapterMap.remove(userId);
    if (adapter != null) {
      ProtocolLibrary.getProtocolManager().removePacketListener(adapter);
    }
    PrintStream stream = packetLogStreams.remove(userId);
    if (stream != null) {
      stream.flush();
      stream.close();
    }
    File packetLogFile = packetLogFiles.remove(userId);
    if (packetLogFile == null || !packetLogFile.isFile()) {
      return Collections.emptyList();
    }
    try {
      return readPacketLog(packetLogFile);
    } catch (IOException exception) {
      exception.printStackTrace();
      return Collections.emptyList();
    }
  }

  private static List<String> readPacketLog(File packetLogFile) throws IOException {
    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(packetLogFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }
    }
    return lines;
  }

  public void logSystemMessage(User target, Supplier<String> messageSupplier) {
    if (target == null) {
      return;
    }
    PrintStream stream;
    try {
      stream = packetLogStreams.get(target.player().getUniqueId());
      if (stream == null) {
        return;
      }
    } catch (Exception exception) {
      return;
    }
    synchronized (stream) {
      stream.println((System.currentTimeMillis() % 1000) + " " + messageSupplier.get());
    }
  }

  private static String packetContent(PacketContainer packet, User receiver) {
    if (packet == null) {
      return "null";
    }
    String typeName = packet.getType().name();
    String[] array = packet.getModifier().getValues().stream()
      .map(PacketLogging::stringFromType)
      .filter(s -> !s.isEmpty())
      .toArray(String[]::new);
    if (typeName.toUpperCase().contains("ENTITY")) {
      Integer entityId = packet.getIntegers().readSafely(0);
      if (entityId != null) {
        array[0] = receiver.meta().connection().entityBy(entityId) + "";
      }
    }
    StringBuilder extra = new StringBuilder();
    if (typeName.equalsIgnoreCase("ENTITY_VELOCITY")) {
      // convert
      try {
        double x = packet.getIntegers().readSafely(1) / 8000.0;
        double y = packet.getIntegers().readSafely(2) / 8000.0;
        double z = packet.getIntegers().readSafely(3) / 8000.0;
        extra.append("x=").append(x).append(", y=").append(y).append(", z=").append(z);
      } catch (Exception exception) {
        // ignore
      }
    }
    return "{" + String.join(", ", array) + "}" + (extra.length() == 0 ? "" : " [" + extra + "]");
  }

  private static String stringFromType(Object object) {
    if (object == null) {
      return "null";
    } else if (object instanceof Number) {
      return object.toString();
    } else if (object instanceof String) {
      return "\"" + object + "\"";
    } else if (object instanceof Boolean) {
      return object.toString();
    } else if (object instanceof byte[]) {
      byte[] bytes = (byte[]) object;
      if (bytes.length == 0) {
        return "[]";
      } else {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int limit = Math.min(bytes.length, 40);
        for (int i = 0; i < limit; i++) {
          builder.append(bytes[i]);
          if (i != limit - 1) {
            builder.append(", ");
          }
        }
        if (bytes.length > 40) {
          builder.append("...");
        }
        builder.append("]");
        return builder.toString();
      }
    } else if (object instanceof int[]) {
      int[] ints = (int[]) object;
      if (ints.length == 0) {
        return "[]";
      } else {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int limit = Math.min(ints.length, 40);
        for (int i = 0; i < limit; i++) {
          builder.append(ints[i]);
          if (i != limit - 1) {
            builder.append(", ");
          }
        }
        if (ints.length > 40) {
          builder.append("...");
        }
        builder.append("]");
        return builder.toString();
      }
    } else if (object instanceof Object[]) {
      Object[] objects = (Object[]) object;
      if (objects.length == 0) {
        return "[]";
      } else {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int limit = Math.min(objects.length, 40);
        for (int i = 0; i < limit; i++) {
          builder.append(stringFromType(objects[i]));
          if (i != limit - 1) {
            builder.append(", ");
          }
        }
        if (objects.length > 40) {
          builder.append("...");
        }
        builder.append("]");
        return builder.toString();
      }
    } else if (object instanceof Collection) {
      Collection<?> collection = (Collection<?>) object;
      if (collection.isEmpty()) {
        return "[]";
      } else {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int limit = Math.min(collection.size(), 40);
        int i = 0;
        for (Object o : collection) {
          builder.append(stringFromType(o));
          if (i != limit - 1) {
            builder.append(", ");
          }
          i++;
        }
        if (collection.size() > 40) {
          builder.append("...");
        }
        builder.append("]");
        return builder.toString();
      }
    } else if (object instanceof Map) {
      Map<?, ?> map = (Map<?, ?>) object;
      if (map.isEmpty()) {
        return "{}";
      } else {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int limit = Math.min(map.size(), 40);
        int i = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
          builder.append(stringFromType(entry.getKey()));
          builder.append("=");
          builder.append(stringFromType(entry.getValue()));
          if (i != limit - 1) {
            builder.append(", ");
          }
          i++;
        }
        if (map.size() > 40) {
          builder.append("...");
        }
        builder.append("}");
        return builder.toString();
      }
    } else if (object.toString().contains("DataWatcher@")) {
      //      WrappedDataWatcher watcher = new WrappedDataWatcher(object);
      //      return "DataWatcher{" + watcher.getWatchableObjects().stream().map(watchableObject -> {
      //        String value = stringFromType(watchableObject.getValue());
      //        return watchableObject.getIndex() + "=" + value;
      //      }).collect(Collectors.joining(", ")) + "}";
      return "DataWatcher{...}";
    } else if (object.toString().contains("WatchableObject@")) {
      //      WrappedDataWatcher.WrappedDataWatcherObject watcherObject = new WrappedDataWatcher.WrappedDataWatcherObject(object);
      //      return "WatchableObject{" + watcherObject.getIndex() + "=" + stringFromType(watcherObject.getHandle()) + "}";
      return "WatchableObject{...}";
    } else if (object.toString().contains("MovingObjectPositionBlock@")) {
      MovingObjectPosition position = MovingObjectPosition.fromNativeMovingObjectPosition(
        object
      );
      return position.toString();
    } else {
      return object.toString();
    }
  }

  private static final DateTimeFormatter FILE_MESSAGE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");

  private static String packetLogFileName(String playername) {
    return "intave-packetlog-" + playername + "-" + LocalDateTime.now().format(FILE_MESSAGE_DATE_FORMATTER).toLowerCase(Locale.ROOT) + ".txt";
  }
}
