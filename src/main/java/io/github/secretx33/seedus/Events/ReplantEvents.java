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
package io.github.secretx33.seedus.Events;

import com.google.common.base.Preconditions;
import io.github.secretx33.seedus.Config.Config;
import io.github.secretx33.seedus.Config.Const;
import io.github.secretx33.seedus.Cuboid;
import io.github.secretx33.seedus.Main;
import io.github.secretx33.seedus.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Locale;

@ParametersAreNonnullByDefault
public class ReplantEvents implements Listener {

    private final Main plugin;

    public ReplantEvents(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @NotNull
    private Location getLowerBounds(Location itemLocation){
        Preconditions.checkNotNull(itemLocation.getWorld(), "Location argument does not have a set world");
        final double x = itemLocation.getBlockX() - Config.getReplantRadius();
        final double y = Math.max(Const.LOWER_LIMIT, Math.round(Math.min(Const.UPPER_LIMIT, itemLocation.getY() - 1)));
        final double z = itemLocation.getBlockZ() - Config.getReplantRadius();
        //Utils.debugMessage(String.format("New LowerBound is %s, %s, %s", x, y, z));
        return new Location(itemLocation.getWorld(), x,y,z);
    }

    @NotNull
    private Location getUpperBounds(Location itemLocation){
        Preconditions.checkNotNull(itemLocation.getWorld(), "Location argument does not have a set world");
        final double x = itemLocation.getBlockX() + Config.getReplantRadius();
        final double y = Math.max(Const.LOWER_LIMIT, Math.round(Math.min(Const.UPPER_LIMIT, itemLocation.getY() - 1)));
        final double z = itemLocation.getBlockZ() + Config.getReplantRadius();
        //Utils.debugMessage(String.format("New UpperBound is %s, %s, %s", x, y, z));
        return new Location(itemLocation.getWorld(),x,y,z);
    }

    @NotNull
    private Location getLocationOnTop(Location location){
        return new Location(location.getWorld(), location.getX(), Math.min(Const.UPPER_LIMIT, location.getY() + 1), location.getZ());
    }

    @NotNull
    private Block getBlockOnTop(Block block){
        return block.getWorld().getBlockAt(getLocationOnTop(block.getLocation()));
    }

    @Nullable
    private Material getBlockForCrop(Material m){
        if(m == Material.WHEAT_SEEDS) return Material.WHEAT;
        if(m == Material.PUMPKIN_SEEDS) return Material.PUMPKIN_STEM;
        if(m == Material.BEETROOT_SEEDS) return Material.BEETROOTS;
        if(m == Material.MELON_SEEDS) return Material.MELON_STEM;
        if(m == Material.CARROT) return Material.CARROTS;
        if(m == Material.COCOA_BEANS) return Material.COCOA;
        if(m == Material.POTATO) return Material.POTATOES;
        return null;
    }

    private void replant(Block block, ItemStack item){
        Preconditions.checkArgument(block.getType() == Material.FARMLAND, "Block passed as argument is not a Farmland block");
        Utils.debugMessage(String.format("Farmland found at %s %s %s", block.getX(), block.getY(), block.getZ()));

        final Material cropBlockType = getBlockForCrop(item.getType());
        if(cropBlockType != null){
            block.getWorld().getBlockAt(getLocationOnTop(block.getLocation())).setType(cropBlockType);
        }
    }

    private boolean isBlockValid(Block block){
        return block.getType() == Material.FARMLAND && getBlockOnTop(block).getType().isAir();
    }

    private void checkItemDropped(Item itemDrop, ItemStack item){
        final String itemName = item.getType().name().toLowerCase(Locale.US);
        if(itemDrop.getLocation().getWorld() == null){
            Utils.debugMessage("Item location does not have a world");
            return;
        }

        if(getBlockForCrop(item.getType()) != null){
            // Check if there is any liquid nearby the item the moment the item spawns, in a plus sign pattern (not checking diagonally)
            final int range = 1;
            for(int x = itemDrop.getLocation().getBlockX() - range; x <= itemDrop.getLocation().getBlockX() + range; x++){
                for(int y = (int)Math.round(Math.max(Const.LOWER_LIMIT, Math.min(Const.UPPER_LIMIT, Math.round(itemDrop.getLocation().getY())))); y <= Math.min(Const.UPPER_LIMIT, Math.round(itemDrop.getLocation().getY() + 1)); y++) {
                    if (itemDrop.getWorld().getBlockAt(x, y, itemDrop.getLocation().getBlockZ()).isLiquid()) {
                        Utils.debugMessage(String.format("There are liquid around this location in block %s, %s, %s", x, y, itemDrop.getLocation().getBlockZ()));
                        return;
                    }
                }
            }

            for(int y = (int)Math.round(Math.max(Const.LOWER_LIMIT, Math.min(Const.UPPER_LIMIT, Math.round(itemDrop.getLocation().getY())))); y <= Math.min(Const.UPPER_LIMIT, Math.round(itemDrop.getLocation().getY() + 1)); y++) {
                for (int z = itemDrop.getLocation().getBlockZ() - range; z <= itemDrop.getLocation().getBlockZ() + range; z++) {
                    if (itemDrop.getWorld().getBlockAt(itemDrop.getLocation().getBlockX(), y, z).isLiquid()) {
                        Utils.debugMessage(String.format("There are liquid around this location in block %s, %s, %s", itemDrop.getLocation().getBlockX(), y, z));
                        return;
                    }
                }
            }

            new BukkitRunnable(){
                @Override
                public void run() {
                    final Location itemLocation = itemDrop.getLocation();
                    Utils.debugMessage(String.format("Item dropped at %s %s %s (%s %s %s)", Math.round(itemLocation.getX()*100f)/100f, Math.round(itemLocation.getY()*100f)/100f, Math.round(itemLocation.getZ()*100f)/100f, itemLocation.getBlockX(), itemLocation.getBlockY(), itemLocation.getBlockZ()));
                    final Cuboid cuboid = new Cuboid(getLowerBounds(itemLocation), getUpperBounds(itemLocation), itemLocation);
                    List<Block> blocks = cuboid.blockList();
                    for(Block b : blocks){
                        if(isBlockValid(b)){
                            replant(b, item);
                            itemDrop.getItemStack().setAmount(itemDrop.getItemStack().getAmount() - 1);
                            if(itemDrop.getItemStack().getAmount() == 0) return;
                        }
                    }
                }
            }.runTaskLater(plugin, (long)(plugin.getTps() * Config.getReplantDelay()));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onItemSpawn(ItemSpawnEvent event){
        checkItemDropped(event.getEntity(), event.getEntity().getItemStack());
    }
}
