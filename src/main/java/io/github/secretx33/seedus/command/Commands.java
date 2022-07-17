/*
This file is part of Seedus (github.com/SecretX33/Seedus).

Seedus is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Seedus is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Seedus.  If not, see <https://www.gnu.org/licenses/>.
*/
package io.github.secretx33.seedus.command;

import io.github.secretx33.seedus.config.Config;
import io.github.secretx33.seedus.model.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Commands implements TabExecutor {

    private static final String PLUGIN_COMMAND_NAME = "seedus";
    private static final String PLUGIN_PERMISSION_PREFIX = "seedus";
    private static final List<String> SUBCOMMANDS = Arrays.asList("reload", "debug");

    private final Config config;

    @Inject
    public Commands(JavaPlugin plugin, Config config) {
        this.config = config;
        PluginCommand command = plugin.getCommand(PLUGIN_COMMAND_NAME);
        if (command == null) {
            throw new IllegalStateException("Command '" + PLUGIN_COMMAND_NAME + "' is not registered.");
        }
        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (strings.length == 0) return true;

        String subCommand = strings[0].toLowerCase(Locale.US);

        switch (subCommand) {
            case "reload":
                if (sender.hasPermission(PLUGIN_PERMISSION_PREFIX + ".reload")) {
                    config.reloadConfig(true);
                    sender.sendMessage(Messages.CONFIGS_RELOADED);
                }
                break;
            case "debug":
                if (sender.hasPermission(PLUGIN_PERMISSION_PREFIX + ".debug")) {
                    config.setDebug(!config.isDebug());
                    String debugState = (config.isDebug()) ? "ON" : "OFF";
                    sender.sendMessage(String.format(Messages.DEBUG_MODE_STATE_CHANGED, debugState));
                }
                break;
            default:
                sender.sendMessage(String.format(Messages.UNKNOWN_COMMAND, subCommand));
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        String hint = args[0].toLowerCase(Locale.US);
        return SUBCOMMANDS.stream()
            .filter(subCommand -> subCommand.startsWith(hint))
            .collect(Collectors.toList());
    }

}
