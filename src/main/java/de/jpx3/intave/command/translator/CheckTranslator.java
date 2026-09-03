package de.jpx3.intave.command.translator;

import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.check.Check;
import de.jpx3.intave.command.TypeTranslator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class CheckTranslator extends TypeTranslator<Check> {
  public CheckTranslator() {
    super(Check.class);
  }

  @Override
  public Check resolve(CommandSender commandSender, String element, String forward) {
    IntavePlugin plugin = IntavePlugin.singletonInstance();
    if (!plugin.checks().hasCheck(element)) {
      commandSender.sendMessage(IntavePlugin.prefix() + ChatColor.RED + "Invalid argument \"" + element + "\": Unable to locate check");
      return null;
    }
    return plugin.checks().searchCheck(element);
  }

  @Override
  public List<String> settingConstrains(CommandSender commandSender) {
    return IntavePlugin.singletonInstance().checks().checks().stream()
      .map(check -> check.name().toLowerCase(Locale.ROOT))
      .collect(Collectors.toList());
  }
}
