package me.vuxaer.tikkertje;

import me.vuxaer.tikkertje.command.TikkertjeCommand;
import me.vuxaer.tikkertje.listener.QuitListener;
import me.vuxaer.tikkertje.listener.RegionListener;
import me.vuxaer.tikkertje.listener.TagListener;
import me.vuxaer.tikkertje.manager.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class Tikkertje extends JavaPlugin {

    private static Tikkertje instance;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.gameManager = new GameManager();

        String region = getConfig().getString("region");
        if (region != null && !region.equalsIgnoreCase("null")) {
            gameManager.setRegion(region);
        }

        getCommand("tikkertje").setExecutor(new TikkertjeCommand(gameManager));

        registerListeners();
        getLogger().info("Tikkertje plugin enabled!");
    }

    public static Tikkertje getInstance() {
        return instance;
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new TagListener(gameManager), this);
        Bukkit.getPluginManager().registerEvents(new RegionListener(gameManager), this);
        Bukkit.getPluginManager().registerEvents(new QuitListener(gameManager), this);
    }
}