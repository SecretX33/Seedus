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
package io.github.secretx33.seedus.eventlistener;

import com.google.common.base.Preconditions;
import io.github.secretx33.seedus.config.Config;
import io.github.secretx33.seedus.model.Cuboid;
import io.github.secretx33.seedus.util.PluginLogger;
import io.github.secretx33.seedus.util.TpsWatcher;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ReplantEventListener implements Listener {

    private static final double LOWER_LIMIT = 0;
    private static final double UPPER_LIMIT = 255;

    private final Plugin plugin;
    private final BukkitScheduler scheduler;
    private final Config config;
    private final PluginLogger log;
    private final TpsWatcher tpsWatcher;

    @Inject
    public ReplantEventListener(Plugin plugin, Config config, PluginLogger log, TpsWatcher tpsWatcher) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
        this.config = config;
        this.log = log;
        this.tpsWatcher = tpsWatcher;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onItemSpawn(ItemSpawnEvent event) {
        checkItemDropped(event.getEntity(), event.getEntity().getItemStack());
    }

    private void checkItemDropped(Item itemDrop, ItemStack item) {
        if (itemDrop.getLocation().getWorld() == null) {
            log.messageDebug("Item location does not have a world");
            return;
        }

        final Material cropMaterial = getBlockForCrop(item.getType());
        if (cropMaterial == null) {
            // Item is not a seed/crop, just silently return
            return;
        }
        if (isWaterNearby(itemDrop)) {
            // Do not plant when water is nearby
            return;
        }

        final long now = System.nanoTime();
        final long delayInTicks = (long) (Math.ceil(config.getReplantDelay() * (double) tpsWatcher.currentTps()) + 1.0);
        scheduler.runTaskLater(plugin, () -> replantCropSeeds(itemDrop, item, now), delayInTicks);
    }

    /**
     * Check if there is any liquid nearby the item the moment the item spawns, in a plus sign pattern
     * (not checking diagonally).
     */
    private boolean isWaterNearby(Item itemDrop) {
        final int range = 1;

        for (int x = itemDrop.getLocation().getBlockX() - range; x <= itemDrop.getLocation().getBlockX() + range; x++) {
            for (int y = (int)Math.round(Math.max(LOWER_LIMIT, Math.min(UPPER_LIMIT, Math.round(itemDrop.getLocation().getY())))); y <= Math.min(UPPER_LIMIT, Math.round(itemDrop.getLocation().getY() + 1)); y++) {
                if (itemDrop.getWorld().getBlockAt(x, y, itemDrop.getLocation().getBlockZ()).isLiquid()) {
                    log.messageDebug("There are liquid around this location in block %s, %s, %s", x, y, itemDrop.getLocation().getBlockZ());
                    return true;
                }
            }
        }

        for (int y = (int)Math.round(Math.max(LOWER_LIMIT, Math.min(UPPER_LIMIT, Math.round(itemDrop.getLocation().getY())))); y <= Math.min(UPPER_LIMIT, Math.round(itemDrop.getLocation().getY() + 1)); y++) {
            for (int z = itemDrop.getLocation().getBlockZ() - range; z <= itemDrop.getLocation().getBlockZ() + range; z++) {
                if (itemDrop.getWorld().getBlockAt(itemDrop.getLocation().getBlockX(), y, z).isLiquid()) {
                    log.messageDebug("There are liquid around this location in block %s, %s, %s", itemDrop.getLocation().getBlockX(), y, z);
                    return true;
                }
            }
        }
        return false;
    }

    private void replantCropSeeds(Item itemDrop, ItemStack item, long start) {
        final Location itemLocation = itemDrop.getLocation();
        log.messageDebug("Item dropped at %s %s %s (%s %s %s)", Math.round(itemLocation.getX()*100f)/100f, Math.round(itemLocation.getY()*100f)/100f, Math.round(itemLocation.getZ()*100f)/100f, itemLocation.getBlockX(), itemLocation.getBlockY(), itemLocation.getBlockZ());

        final Cuboid cuboid = new Cuboid(getLowerBounds(itemLocation), getUpperBounds(itemLocation), itemLocation);
        final List<Block> blocks = cuboid.blockList();

        for (Block block : blocks) {
            if (isBlockValid(block)) {
                replant(block, item);
                final ItemStack itemStack = itemDrop.getItemStack();
                itemStack.setAmount(itemStack.getAmount() - 1);
                if (itemStack.getAmount() == 0) {
                    // There is no more seeds to be planted
//                    itemDrop.remove();
                    break;
                }
            }
        }
        log.messageDebug("Replanting task ran after %.2fs. TPS: %s", (System.nanoTime() - start) / 1_000_000_000.0, tpsWatcher.currentTps());
    }

    private Location getLowerBounds(Location itemLocation) {
        return getBound(itemLocation, -config.getReplantRadius());
    }

    private Location getUpperBounds(Location itemLocation) {
        return getBound(itemLocation, config.getReplantRadius());
    }

    private Location getBound(Location itemLocation, int offset) {
        final World world = checkNotNull(itemLocation.getWorld(), "Location argument does not have a set world");
        final double x = itemLocation.getBlockX() + offset;
        final double y = Math.max(LOWER_LIMIT, Math.round(Math.min(UPPER_LIMIT, itemLocation.getY() - 1)));
        final double z = itemLocation.getBlockZ() + offset;
        return new Location(world, x, y, z);
    }

    private void replant(Block block, ItemStack item) {
        Preconditions.checkArgument(block.getType() == Material.FARMLAND, "Block passed as argument is not a Farmland block");
        log.messageDebug("Farmland found at %s %s %s", block.getX(), block.getY(), block.getZ());

        final Material cropBlockType = getBlockForCrop(item.getType());
        if (cropBlockType != null) {
            block.getWorld().getBlockAt(getLocationOnTop(block.getLocation())).setType(cropBlockType);
        }
    }

    @Nullable
    private Material getBlockForCrop(Material material) {
        if (material == Material.WHEAT_SEEDS) return Material.WHEAT;
        if (material == Material.PUMPKIN_SEEDS) return Material.PUMPKIN_STEM;
        if (material == Material.BEETROOT_SEEDS) return Material.BEETROOTS;
        if (material == Material.MELON_SEEDS) return Material.MELON_STEM;
        if (material == Material.CARROT) return Material.CARROTS;
        if (material == Material.COCOA_BEANS) return Material.COCOA;
        if (material == Material.POTATO) return Material.POTATOES;
        return null;
    }

    private boolean isBlockValid(Block block) {
        return block.getType() == Material.FARMLAND && getBlockOnTop(block).getType().isAir();
    }

    private Block getBlockOnTop(Block block) {
        return block.getWorld().getBlockAt(getLocationOnTop(block.getLocation()));
    }

    private Location getLocationOnTop(Location location) {
        return new Location(location.getWorld(), location.getX(), Math.min(UPPER_LIMIT, location.getY() + 1), location.getZ());
    }
}
