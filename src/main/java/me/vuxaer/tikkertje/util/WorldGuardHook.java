package me.vuxaer.tikkertje.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WorldGuardHook {

    public static boolean isInRegion(Player player, String region) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            return query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()))
                    .getRegions()
                    .stream()
                    .anyMatch(r -> r.getId().equalsIgnoreCase(region));
        } catch (NoClassDefFoundError e) {
            return true;
        }
    }

    public static boolean regionExists(World world, String regionName) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager manager = container.get(BukkitAdapter.adapt(world));
            if (manager == null) return false;

            ProtectedRegion region = manager.getRegion(regionName);
            return region != null;

        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    public static List<String> getRegions(World world) {
        List<String> regions = new ArrayList<>();

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager manager = container.get(BukkitAdapter.adapt(world));

            if (manager == null) return regions;

            for (ProtectedRegion region : manager.getRegions().values()) {
                regions.add(region.getId());
            }

        } catch (NoClassDefFoundError e) {
        }

        return regions;
    }
}