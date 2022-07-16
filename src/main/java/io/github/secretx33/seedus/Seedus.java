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
package io.github.secretx33.seedus;

import io.github.secretx33.seedus.command.Commands;
import io.github.secretx33.seedus.eventlistener.ReplantEventListener;
import io.github.secretx33.seedus.util.PluginLogger;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.configuration.Configuration;

public class Seedus extends JavaPlugin {

    private PluginLogger log;

    @Override
    public void onEnable() {
        JavaPlugin plugin = this;
        Toothpick.setConfiguration(Configuration.forProduction());
        Scope scope = Toothpick.openScope(this, inject -> inject.installModules(new Module() {{
            bind(Plugin.class).toInstance(plugin);
            bind(JavaPlugin.class).toInstance(plugin);
            bind(ConsoleCommandSender.class).toInstance(getServer().getConsoleSender());
            bind(Scope.class).toInstance(inject);
        }}));
        log = scope.getInstance(PluginLogger.class);

        registerEventListeners(scope.getInstance(ReplantEventListener.class));
        scope.getInstance(Commands.class);
        log.message("loaded.");
    }

    private void registerEventListeners(Listener... eventListeners) {
        PluginManager pluginManager = getServer().getPluginManager();
        for (Listener listener : eventListeners) {
            pluginManager.registerEvents(listener, this);
        }
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        Toothpick.closeScope(this);
        log.message("disabled.");
    }

}
