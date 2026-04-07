package me.vuxaer.tikkertje.command;

import me.vuxaer.tikkertje.manager.GameManager;
import me.vuxaer.tikkertje.util.GameState;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TikkertjeCommand implements CommandExecutor {

    private final GameManager gameManager;

    public TikkertjeCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    private List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (gameManager.getState() == GameState.RUNNING) {
            player.sendMessage("§cGame is al bezig!");
            return true;
        }

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        if (players.size() < 2) {
            player.sendMessage("§cMinimaal 2 spelers nodig!");
            return true;
        }

        gameManager.startGame(players);

        return true;
    }
}