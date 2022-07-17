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
package io.github.secretx33.seedus.util;

import io.github.secretx33.seedus.config.Config;
import io.github.secretx33.seedus.model.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import toothpick.Scope;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PluginLogger {

    static final String CHAT_CONSOLE_PREFIX = ChatColor.DARK_GREEN + "[" + Messages.PLUGIN_NAME + "] " + ChatColor.GRAY;

    /**
     * Lazy (pun not intended) fix for the circular dependency issue.
     */
    private final Lazy<Config> config;
    private final ConsoleCommandSender console;

    @Inject
    public PluginLogger(Scope scope, ConsoleCommandSender console) {
        this.config = new Lazy<>(() -> scope.getInstance(Config.class));
        this.console = console;
    }

    public void message(String msg) {
        console.sendMessage(CHAT_CONSOLE_PREFIX + msg);
    }

    public void message(String msg, Object... args) {
        message(String.format(msg, args));
    }

    public void messageDebug(String msg) {
        if (isDebugDisabled()) return;
        message(msg);
    }

    public void messageDebug(String msg, Object... args) {
        if (isDebugDisabled()) return;
        message(msg, args);
    }

    private boolean isDebugDisabled() {
        return !config.get().isDebug();
    }
}
