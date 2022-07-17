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

import org.bukkit.Bukkit;
import toothpick.configuration.Configuration;

import java.util.Optional;

public final class ConfigurationUtil {

    private static final boolean IS_DEVELOPMENT;

    static {
        boolean isDevelopment = false;
        try {
            isDevelopment = Optional.ofNullable(System.getenv("BUKKIT_DEVELOPMENT"))
                .map(Boolean::parseBoolean)
                .orElse(false);
        } catch (Exception ignored) {
        }
        IS_DEVELOPMENT = isDevelopment;
    }

    private ConfigurationUtil() {}

    public static Configuration getEnvConfiguration() {
        if (IS_DEVELOPMENT) {
            Bukkit.getServer().getConsoleSender().sendMessage(PluginLogger.CHAT_CONSOLE_PREFIX + "Running in development mode.");
        }
        return IS_DEVELOPMENT ? Configuration.forDevelopment() : Configuration.forProduction();
    }
}
