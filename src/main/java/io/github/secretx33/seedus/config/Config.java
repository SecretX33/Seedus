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
package io.github.secretx33.seedus.config;

import io.github.secretx33.seedus.model.Messages;
import io.github.secretx33.seedus.util.PluginLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Config {

    private final Plugin plugin;
    private final PluginLogger log;

    private double replantDelay;
    private int replantRadius;
    private boolean debug = false;

    @Inject
    public Config(Plugin plugin, PluginLogger log) {
        this.plugin = plugin;
        this.log = log;
        reloadConfig(false);
    }

    public void reloadConfig(boolean async) {
        if (async) {
            doAsync(this::reloadConfigInternal);
        } else {
            reloadConfigInternal();
        }
    }

    private void reloadConfigInternal() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        String section = "general";
        ConfigurationSection general = null;
        if (config.isSet(section)) {
            general = config.getConfigurationSection(section);
        }
        if (general == null) {
            log.message(Messages.SECTION_NOT_FOUND, section);
            return;
        }

        // Parsing configs
        String field = "time-to-replant";
        replantDelay = 1.5;
        if (general.isSet(field)) {
            replantDelay = Math.max(0.5, Math.min(10.0, general.getDouble(field)));
        } else {
            log.message(Messages.ENTRY_NOT_FOUND, field);
        }

        field = "radius-to-replant";
        replantRadius = 2;
        if (general.isSet(field)) {
            replantRadius = Math.max(0, Math.min(20, general.getInt(field)));
        } else {
            log.message(Messages.ENTRY_NOT_FOUND, field);
        }

        if (config.isSet("general.debug")) {
            debug = config.getBoolean("general.debug");
        }
    }

    public double getReplantDelay() {
        return replantDelay;
    }

    public int getReplantRadius() {
        return replantRadius;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        plugin.getConfig().set("general.debug", debug);
        doAsync(plugin::saveConfig);
    }

    private void doAsync(Runnable runnable) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }
}
