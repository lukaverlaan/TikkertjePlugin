package me.vuxaer.tikkertje;

import me.vuxaer.tikkertje.command.TikkertjeCommand;
import me.vuxaer.tikkertje.command.TikkertjeTabCompleter;
import me.vuxaer.tikkertje.listener.QuitListener;
import me.vuxaer.tikkertje.listener.RegionListener;
import me.vuxaer.tikkertje.listener.TagListener;
import me.vuxaer.tikkertje.manager.GameManager;
import me.vuxaer.tikkertje.manager.SpawnManager;
import me.vuxaer.tikkertje.net.HttpService;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Tikkertje extends JavaPlugin {

    private static Tikkertje instance;

    private GameManager gameManager;
    private SpawnManager spawnManager;
    private HttpService httpService;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.spawnManager = new SpawnManager(this);
        this.gameManager = new GameManager();
        this.httpService = new HttpService(this);

        String region = getConfig().getString("region");
        if (region != null && !region.isEmpty()) {
            gameManager.setRegion(region);
        }

        PluginCommand command = getCommand("tikkertje");
        if (command != null) {
            command.setExecutor(new TikkertjeCommand(gameManager));
            command.setTabCompleter(new TikkertjeTabCompleter(gameManager));
        }

        registerListeners();
    }

    public static Tikkertje getInstance() {
        return instance;
    }

    private void registerListeners() {
        var pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new TagListener(gameManager), this);
        pluginManager.registerEvents(new RegionListener(gameManager), this);
        pluginManager.registerEvents(new QuitListener(gameManager), this);
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public HttpService getHttpService() {
        return httpService;
    }
}