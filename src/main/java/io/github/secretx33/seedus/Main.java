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

import io.github.secretx33.seedus.Commands.Commands;
import io.github.secretx33.seedus.Config.Config;
import io.github.secretx33.seedus.Events.ReplantEvents;
import io.github.secretx33.seedus.Utils.Utils;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private int tps = 0;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Utils.init(this);
        Config.setPlugin(this);
        Config.reloadConfig();
        Commands cmds = new Commands(this);
        ReplantEvents replantEvents = new ReplantEvents(this);

        // Measuring TPS
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            long sec;
            long currentSec;
            int ticks;
            int delay;

            @Override
            public void run() {
                sec = (System.currentTimeMillis() / 1000);

                if (currentSec == sec) { // this code block triggers each tick
                    ticks++;
                } else { // this code block triggers each second
                    currentSec = sec;
                    tps = (tps == 0 ? ticks : ((tps + ticks) / 2));
                    ticks = 0;

                    if ((++delay % 300) == 0) {// this code block triggers each 5 minutes
                        delay = 0;
                    }
                }
            }
        }, 0, 1); // do not change the "1" value, the other one is just initial delay, I recommend 0 = start instantly.
        Utils.consoleMessage("loaded.");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        Utils.consoleMessage("disabled.");
    }

    public int getTps() {
        return tps;
    }
}
