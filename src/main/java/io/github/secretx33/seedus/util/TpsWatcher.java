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

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TpsWatcher {

    private final Plugin plugin;
    private final BukkitScheduler scheduler;
    private int currentTps = 0;

    @Inject
    public TpsWatcher(Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
        startTpsWatcherTask();
    }

    private void startTpsWatcherTask() {
        Runnable runnable = new Runnable() {
            long sec;
            long currentSec;
            int ticks;
            int delay;

            @Override
            public void run() {
                sec = (System.currentTimeMillis() / 1000);

                if (currentSec == sec) {
                    // this code block triggers each tick
                    ticks++;
                } else {
                    // this code block triggers each second
                    currentSec = sec;
                    currentTps = (currentTps == 0 ? ticks : ((currentTps + ticks) / 2));
                    ticks = 0;

                    if ((++delay % 300) == 0) {
                        // this code block triggers each 5 minutes
                        delay = 0;
                    }
                }
            }
        };
        scheduler.runTaskTimer(plugin, runnable, 0L, 1L);
    }

    public int currentTps() {
        return currentTps;
    }
}
