package me.vuxaer.tikkertje;

import me.vuxaer.tikkertje.command.TikkertjeCommand;
import me.vuxaer.tikkertje.listener.TagListener;
import me.vuxaer.tikkertje.manager.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Tikkertje extends JavaPlugin {

    private static Tikkertje instance;

    private GameManager gameManager;

    @Override
    public void onEnable() {
        instance = this;

        this.gameManager = new GameManager();

        getCommand("tikkertje").setExecutor(new TikkertjeCommand(gameManager));

        Bukkit.getPluginManager().registerEvents(new TagListener(gameManager), this);

        getLogger().info("Tikkertje plugin enabled!");
    }

    public static Tikkertje getInstance() {
        return instance;
    }

    public GameManager getGameManager() {
        return gameManager;
    }
}