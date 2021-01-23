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
package io.github.secretx33.seedus.Config;

import io.github.secretx33.seedus.Main;
import io.github.secretx33.seedus.Utils.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class Config {
    private static Main plugin;
    private static double replantDelay;
    private static int replantRadius;
    private static boolean debug = false;

    private Config() {}

    public static void reloadConfig(){
        if(plugin == null) {
            throw new NullPointerException("Plugin variable was not set yet.");
        }
        FileConfiguration config = plugin.getConfig();
        String section = "general";
        ConfigurationSection general = null;
        if(config.isSet(section)){
            general = config.getConfigurationSection(section);
        }
        if (general == null) {
            Utils.consoleMessage(String.format(Const.SECTION_NOT_FOUND, section));
            return;
        }

        // Parsing configs
        String field = "time-to-replant";
        replantDelay = 1.2;
        if(general.isSet(field)){
            replantDelay = Math.max(0.5, Math.min(10.0, general.getDouble(field)));
        } else {
            Utils.consoleMessage(String.format(Const.ENTRY_NOT_FOUND, field));
        }

        field = "radius-to-replant";
        replantRadius = 3;
        if(general.isSet(field)){
            String s = general.getString(field);
            replantRadius = (int)Math.max(0, Math.min(20, general.getDouble(field)));
        } else {
            Utils.consoleMessage(String.format(Const.ENTRY_NOT_FOUND, field));
        }

        if(config.isSet("general.debug")) debug = config.getBoolean("general.debug");
    }

    public static void setPlugin(@NotNull Main p) {
        plugin = p;
    }

    public static double getReplantDelay() {
        return replantDelay;
    }

    public static int getReplantRadius() {
        return replantRadius;
    }

    public static boolean getDebug(){
        return debug;
    }

    public static void setDebug(boolean debug) {
        Config.debug = debug;
    }
}
