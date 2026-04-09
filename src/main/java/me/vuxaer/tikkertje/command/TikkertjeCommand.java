package me.vuxaer.tikkertje.command;

import me.vuxaer.tikkertje.Tikkertje;
import me.vuxaer.tikkertje.manager.GameManager;
import me.vuxaer.tikkertje.util.GameState;
import me.vuxaer.tikkertje.util.WorldGuardHook;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TikkertjeCommand implements CommandExecutor {

    private final Tikkertje plugin = Tikkertje.getInstance();
    private final GameManager gameManager;

    public TikkertjeCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "help" -> sendHelp(player);
            case "start" -> {
                if (gameManager.getState() == GameState.RUNNING) {
                    player.sendMessage("§cEr is al een spel bezig!");
                    return true;
                }
                List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                if (players.size() < 2) {
                    player.sendMessage("§cMinimaal 2 spelers nodig!");
                    return true;
                }
                gameManager.startGame(players);
            }
            case "setregion" -> {
                if (gameManager.getState() == GameState.RUNNING) {
                    player.sendMessage("§cJe kunt de region niet aanpassen tijdens een spel!");
                    return true;
                }
                if (!gameManager.hasWorldGuard()) {
                    player.sendMessage("§cWorldGuard is niet geïnstalleerd!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cGebruik: /tikkertje setregion <naam>");
                    return true;
                }
                String region = args[1].toLowerCase();
                if (!WorldGuardHook.regionExists(player.getWorld(), region)) {
                    player.sendMessage("§cDeze region bestaat niet!");
                    return true;
                }
                gameManager.setRegion(region);
                plugin.getConfig().set("region", region);
                plugin.saveConfig();
                player.sendMessage("§aRegion ingesteld op: §e" + region);
            }
            case "clearregion" -> {
                if (gameManager.getState() == GameState.RUNNING) {
                    player.sendMessage("§cJe kunt de region niet aanpassen tijdens een spel!");
                    return true;
                }
                if (!gameManager.hasWorldGuard()) {
                    player.sendMessage("§cWorldGuard is niet geïnstalleerd!");
                    return true;
                }
                if (!gameManager.hasRegion()) {
                    player.sendMessage("§cEr is geen region ingesteld!");
                    return true;
                }
                gameManager.setRegion(null);
                plugin.getConfig().set("region", null);
                plugin.saveConfig();
                player.sendMessage("§cRegion verwijderd!");
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(" ");
        sender.sendMessage("§6§lTikkertje Commands");
        sender.sendMessage(" ");
        sender.sendMessage("§e/tikkertje start §7- Start een spel");
        sender.sendMessage("§e/tikkertje help §7- Toon dit menu");
        sender.sendMessage("§e/tikkertje setregion <naam> §7- Stel een region in");
        sender.sendMessage("§e/tikkertje clearregion §7- Verwijder de huidige region");
        if (gameManager.hasRegion()) {
            sender.sendMessage(" ");
            sender.sendMessage("§7Huidige region: §e" + gameManager.getRegion());
        }
        sender.sendMessage(" ");
    }
}