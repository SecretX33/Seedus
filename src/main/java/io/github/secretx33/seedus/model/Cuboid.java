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
package io.github.secretx33.seedus.model;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Cuboid {

    private final int xMin;
    private final int xMax;
    private final int yMin;
    private final int yMax;
    private final int zMin;
    private final int zMax;
    private final double xMinCentered;
    private final double xMaxCentered;
    private final double yMinCentered;
    private final double yMaxCentered;
    private final double zMinCentered;
    private final double zMaxCentered;
    // Reference point inside Cuboid
    private final Location refLocation;
    private final World world;

    public Cuboid(final Location point1, final Location point2, final Location refLocation) {
        this.xMin = Math.min(point1.getBlockX(), point2.getBlockX());
        this.xMax = Math.max(point1.getBlockX(), point2.getBlockX());
        this.yMin = Math.min(point1.getBlockY(), point2.getBlockY());
        this.yMax = Math.max(point1.getBlockY(), point2.getBlockY());
        this.zMin = Math.min(point1.getBlockZ(), point2.getBlockZ());
        this.zMax = Math.max(point1.getBlockZ(), point2.getBlockZ());
        this.world = point1.getWorld();
        this.xMinCentered = this.xMin + 0.5;
        this.xMaxCentered = this.xMax + 0.5;
        this.yMinCentered = this.yMin + 0.5;
        this.yMaxCentered = this.yMax + 0.5;
        this.zMinCentered = this.zMin + 0.5;
        this.zMaxCentered = this.zMax + 0.5;
        this.refLocation = new Location(refLocation.getWorld(), refLocation.getX(), refLocation.getY(), refLocation.getZ());
    }

    public Set<Block> bordersBlockList() {
        final Set<Block> blockList = new LinkedHashSet<>();

        // For all heights
        for (int y = this.yMin; y <= this.yMax ; ++y) {
            // Get North and South Walls
            for (int x = this.xMin; x <= this.xMax; ++x) {
                final Block southBlock = this.world.getBlockAt(x, y, zMin);
                blockList.add(southBlock);

                final Block northBlock = this.world.getBlockAt(x, y, zMax);
                blockList.add(northBlock);
            }

            // Get West and East Walls
            for (int z = this.zMin; z <= this.zMax; ++z) {
                final Block eastBlock = this.world.getBlockAt(xMin, y, z);
                blockList.add(eastBlock);

                final Block westBlock = this.world.getBlockAt(xMax, y, z);
                blockList.add(westBlock);
            }
        }

        return blockList;
    }

    public Set<Block> allSidesBlockList() {
        final Set<Block> blockList = new LinkedHashSet<>();

        // Adding floor and ceil of cube
        for (int x = this.xMin; x <= this.xMax; ++x) {
            for (int z = this.zMin; z <= this.zMax; ++z) {
                final Block floor = this.world.getBlockAt(x, yMin, z);
                blockList.add(floor);

                final Block ceil = this.world.getBlockAt(x, yMax, z);
                blockList.add(ceil);
            }
        }

        // There is no walls on this cuboid, just floor and ceil
        if ((yMax - yMin) < 3) return blockList;

        // For all block in between floor and ceil
        for (int y = this.yMin + 1; y <= this.yMax - 1; ++y) {
            // Get North and South Walls
            for (int x = this.xMin; x <= this.xMax; ++x) {
                final Block southBlock = this.world.getBlockAt(x, y, zMin);
                blockList.add(southBlock);

                final Block northBlock = this.world.getBlockAt(x, y, zMax);
                blockList.add(northBlock);
            }

            // Get West and East Walls
            for (int z = this.zMin; z <= this.zMax; ++z) {
                final Block eastBlock = this.world.getBlockAt(xMin, y, z);
                blockList.add(eastBlock);

                final Block westBlock = this.world.getBlockAt(xMax, y, z);
                blockList.add(westBlock);
            }
        }

        return blockList;
    }

    public List<Block> blockList() {
        final List<Block> blockList = new ArrayList<>(this.getTotalBlockSize());
        for (int x = this.xMin; x <= this.xMax; ++x) {
            for (int y = this.yMin; y <= this.yMax; ++y) {
                for (int z = this.zMin; z <= this.zMax; ++z) {
                    final Block b = this.world.getBlockAt(x, y, z);
                    blockList.add(b);
                }
            }
        }
        // Sort the blocks from the center to the edges
        blockList.sort((o1, o2) -> {
            final double o1DistanceFromRef = getDistanceFromRef(o1.getLocation());
            final double o2DistanceFromRef = getDistanceFromRef(o2.getLocation());
//            log.messageDebug(String.format("[1] Block %s %s %s is %s away from the center\n[2] block %s %s %s is %s away from the center", o1.getX(), o1.getY(), o1.getZ(), o1DistanceFromRef, o2.getX(), o2.getY(), o2.getZ(), o2DistanceFromRef));
            return Double.compare(o1DistanceFromRef, o2DistanceFromRef);
        });
        return blockList;
    }

    private double getDistanceFromRef(Location loc) {
        // 0.9375 is the height of the farmland
        return Math.sqrt(Math.pow((loc.getX() + 0.5) - refLocation.getX(), 2) + Math.pow((loc.getY() + 0.9375) - refLocation.getY(), 2) + Math.pow((loc.getZ() + 0.5) - refLocation.getZ(), 2));
    }

    public Location getCenter() {
        return new Location(this.world, (this.xMax - this.xMin) / 2f + this.xMin, (this.yMax - this.yMin) / 2f + this.yMin, (this.zMax - this.zMin) / 2f + this.zMin);
    }

    public double getDistance() {
        return this.getPoint1().distance(this.getPoint2());
    }

    public double getDistanceSquared() {
        return this.getPoint1().distanceSquared(this.getPoint2());
    }

    public int getHeight() {
        return this.yMax - this.yMin + 1;
    }

    public Location getPoint1() {
        return new Location(this.world, this.xMin, this.yMin, this.zMin);
    }

    public Location getPoint2() {
        return new Location(this.world, this.xMax, this.yMax, this.zMax);
    }

    public Location getRandomLocation() {
        final Random rand = new Random();
        final int x = rand.nextInt(Math.abs(this.xMax - this.xMin) + 1) + this.xMin;
        final int y = rand.nextInt(Math.abs(this.yMax - this.yMin) + 1) + this.yMin;
        final int z = rand.nextInt(Math.abs(this.zMax - this.zMin) + 1) + this.zMin;
        return new Location(this.world, x, y, z);
    }

    public int getTotalBlockSize() {
        return this.getHeight() * this.getXWidth() * this.getZWidth();
    }

    public int getXWidth() {
        return this.xMax - this.xMin + 1;
    }

    public int getZWidth() {
        return this.zMax - this.zMin + 1;
    }

    public boolean isIn(final Location loc) {
        return loc.getWorld() == this.world && loc.getBlockX() >= this.xMin && loc.getBlockX() <= this.xMax && loc.getBlockY() >= this.yMin && loc.getBlockY() <= this.yMax && loc
                .getBlockZ() >= this.zMin && loc.getBlockZ() <= this.zMax;
    }

    public boolean isIn(final Player player) {
        return this.isIn(player.getLocation());
    }

    public boolean isInWithMarge(final Location loc, final double marge) {
        return loc.getWorld() == this.world && loc.getX() >= this.xMinCentered - marge && loc.getX() <= this.xMaxCentered + marge && loc.getY() >= this.yMinCentered - marge && loc
                .getY() <= this.yMaxCentered + marge && loc.getZ() >= this.zMinCentered - marge && loc.getZ() <= this.zMaxCentered + marge;
    }
}
