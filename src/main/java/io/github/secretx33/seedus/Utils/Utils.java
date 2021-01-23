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
package io.github.secretx33.seedus.Utils;

import com.google.common.base.Preconditions;
import io.github.secretx33.seedus.Config.Config;
import io.github.secretx33.seedus.Config.Const;
import io.github.secretx33.seedus.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class Utils {
    private static Main plugin;
    private static ConsoleCommandSender console;

    private Utils() { }

    public static void consoleMessage(String msg){
        Preconditions.checkNotNull(console,"console variable is null");
        console.sendMessage(ChatColor.DARK_GREEN + "[" + Const.PLUGIN_NAME + "] " + ChatColor.GRAY + msg);
    }

    public static void debugMessage(String msg){
        if(!Config.getDebug()) return;
        Preconditions.checkNotNull(console,"console variable is null");
        console.sendMessage(ChatColor.DARK_GREEN + "[" + Const.PLUGIN_NAME + "] " + ChatColor.GRAY + msg);
    }

    public static void init(Main plugin) {
        Utils.plugin = plugin;
        Utils.console = plugin.getServer().getConsoleSender();
    }
}
