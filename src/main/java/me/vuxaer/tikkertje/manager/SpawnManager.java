package me.vuxaer.tikkertje.manager;

import me.vuxaer.tikkertje.Tikkertje;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SpawnManager {

    private final Tikkertje plugin;

    public SpawnManager(Tikkertje plugin) {
        this.plugin = plugin;
    }

    public void setSpawn(String key, Location loc) {
        String path = "spawns." + key;

        plugin.getConfig().set(path + ".world", loc.getWorld().getName());
        plugin.getConfig().set(path + ".x", loc.getX());
        plugin.getConfig().set(path + ".y", loc.getY());
        plugin.getConfig().set(path + ".z", loc.getZ());
        plugin.getConfig().set(path + ".yaw", loc.getYaw());
        plugin.getConfig().set(path + ".pitch", loc.getPitch());
    }

    public Location getSpawn(String key) {
        String path = "spawns." + key;

        if (!plugin.getConfig().contains(path)) return null;

        String world = plugin.getConfig().getString(path + ".world");

        return new Location(
                Bukkit.getWorld(world),
                plugin.getConfig().getDouble(path + ".x"),
                plugin.getConfig().getDouble(path + ".y"),
                plugin.getConfig().getDouble(path + ".z"),
                (float) plugin.getConfig().getDouble(path + ".yaw"),
                (float) plugin.getConfig().getDouble(path + ".pitch")
        );
    }
}